package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.RawAnnouncementDraft;
import com.yaxinaz.ai.dto.RawIssueAIAnalysis;
import com.yaxinaz.ai.dto.RawSearchQueryInterpretation;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.config.properties.AiProperties;
import com.yaxinaz.exception.AIServiceException;
import com.yaxinaz.exception.InvalidAIResponseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Real Claude-backed implementation. Never called directly by controllers/services - always go
 * through {@link CommunityAIServiceRouter}, which decides whether Claude is usable and falls back
 * to {@link MockCommunityAIService} on failure per spec sections 36-37.
 */
@Service
@RequiredArgsConstructor
public class ClaudeCommunityAIService implements CommunityAIService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCommunityAIService.class);

    private static final String SYSTEM_PROMPT = """
            You are an assistant that classifies community maintenance/safety issue reports for a
            residential platform. You NEVER take actions - you only analyze text and return a single
            JSON object matching the requested shape. Respond with JSON only, no markdown fences, no
            commentary.

            Valid category values: ELEVATOR, WATER, ELECTRICITY, GAS, HEATING, PARKING, SECURITY,
            NOISE, CLEANING, WASTE, INTERNET, BUILDING_DAMAGE, ROAD, LIGHTING, ANIMAL,
            ACCESSIBILITY, OTHER.
            Valid prioritySuggestion values: LOW, MEDIUM, HIGH, CRITICAL.
            """;

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final AiProperties aiProperties;

    @Override
    public IssueAIAnalysisResponse analyzeIssue(String rawText, String buildingOrLocation, String language) {
        ChatClient chatClient = requireChatClient();

        String userPrompt = """
                Analyze this community issue report and respond with JSON matching this shape:
                {"title": string, "category": string, "prioritySuggestion": string, "summary": string,
                 "affectedGroups": string[], "tags": string[]}

                Location/building: %s
                Preferred response language: %s
                Report text: "%s"
                """.formatted(
                buildingOrLocation == null || buildingOrLocation.isBlank() ? "not specified" : buildingOrLocation,
                language == null || language.isBlank() ? "EN" : language,
                rawText);

        RawIssueAIAnalysis raw;
        try {
            raw = callWithTimeout(() -> chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(RawIssueAIAnalysis.class));
        } catch (TimeoutException ex) {
            throw new AIServiceException("Claude did not respond within " + aiProperties.timeoutMs() + "ms", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Claude issue analysis failed", ex);
        }

        if (raw == null || raw.title() == null) {
            throw new InvalidAIResponseException("Claude returned an empty or malformed analysis");
        }

        return new IssueAIAnalysisResponse(
                AIResponseValidator.requireNonBlank(raw.title(), "Community issue reported"),
                AIResponseValidator.parseCategory(raw.category()),
                AIResponseValidator.parsePriority(raw.prioritySuggestion()),
                AIResponseValidator.requireNonBlank(raw.summary(), rawText),
                AIResponseValidator.safeList(raw.affectedGroups()),
                AIResponseValidator.safeList(raw.tags()),
                AIProvider.CLAUDE
        );
    }

    @Override
    public String summarizeCommunity(StatisticsResponse stats, String communityName, String language) {
        ChatClient chatClient = requireChatClient();
        String prompt = """
                Write a short (2-4 sentence) plain-language summary of this community's activity this
                period, in %s. Use ONLY the numbers given below - never invent, estimate, or round
                figures that aren't provided. Do not add commentary about data you don't have.

                Community: %s
                Total issues reported: %d
                Open issues: %d
                Resolved issues: %d
                Critical issues: %d
                High priority issues: %d
                Resolution rate: %.2f%%
                Most common category: %s
                """.formatted(
                language == null || language.isBlank() ? "English" : language,
                communityName, stats.totalIssues(), stats.openIssues(), stats.resolvedIssues(),
                stats.criticalIssues(), stats.highPriorityIssues(), stats.resolutionRatePercent(),
                stats.mostCommonCategory() != null ? stats.mostCommonCategory().name() : "none");

        try {
            String content = callWithTimeout(() -> chatClient.prompt().user(prompt).call().content());
            return AIResponseValidator.requireNonBlank(content, "No summary available for this period.");
        } catch (TimeoutException ex) {
            throw new AIServiceException("Claude did not respond within " + aiProperties.timeoutMs() + "ms", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Claude community summary failed", ex);
        }
    }

    @Override
    public List<String> generateInsights(StatisticsResponse stats, String language) {
        if (stats.totalIssues() < 3) {
            return List.of();
        }
        ChatClient chatClient = requireChatClient();
        String prompt = """
                Based ONLY on the numbers below, write 0 to 3 short factual insight sentences in %s
                about this community's issue activity. If the numbers don't support any meaningful
                insight, return an empty array. Never invent a trend or number not given here. Respond
                with a JSON array of strings only, no markdown fences, no commentary.

                Total issues: %d
                Escalated issues: %d
                Resolution rate: %.2f%%
                Most common category: %s
                Issues by category: %s
                """.formatted(
                language == null || language.isBlank() ? "English" : language,
                stats.totalIssues(), stats.escalatedIssues(), stats.resolutionRatePercent(),
                stats.mostCommonCategory() != null ? stats.mostCommonCategory().name() : "none",
                stats.issuesByCategory());

        try {
            String[] result = callWithTimeout(() -> chatClient.prompt().user(prompt).call().entity(String[].class));
            return result == null ? List.of() : AIResponseValidator.safeList(Arrays.asList(result));
        } catch (TimeoutException ex) {
            throw new AIServiceException("Claude did not respond within " + aiProperties.timeoutMs() + "ms", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Claude community insights failed", ex);
        }
    }

    @Override
    public AnnouncementDraftResponse draftAnnouncement(String rawNotes, String postType, String language) {
        ChatClient chatClient = requireChatClient();
        String prompt = """
                Turn these rough notes into a polished, professional community announcement, in %s.
                Keep the tone clear and appropriate for the post type "%s". Do not invent facts,
                dates, or details not present in the notes. Respond with JSON only, no markdown
                fences, no commentary, matching this shape: {"title": string, "content": string}

                Notes: "%s"
                """.formatted(
                language == null || language.isBlank() ? "English" : language,
                postType == null || postType.isBlank() ? "GENERAL" : postType,
                rawNotes);

        RawAnnouncementDraft raw;
        try {
            raw = callWithTimeout(() -> chatClient.prompt().user(prompt).call().entity(RawAnnouncementDraft.class));
        } catch (TimeoutException ex) {
            throw new AIServiceException("Claude did not respond within " + aiProperties.timeoutMs() + "ms", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Claude announcement drafting failed", ex);
        }

        if (raw == null || raw.content() == null) {
            throw new InvalidAIResponseException("Claude returned an empty or malformed announcement draft");
        }

        return new AnnouncementDraftResponse(
                AIResponseValidator.requireNonBlank(raw.title(), "Community Announcement"),
                AIResponseValidator.requireNonBlank(raw.content(), rawNotes),
                AIProvider.CLAUDE
        );
    }

    @Override
    public SearchQueryInterpretation interpretSearchQuery(String query, String language) {
        ChatClient chatClient = requireChatClient();
        String prompt = """
                A resident of a community platform is searching using natural language. Decide which
                result types are relevant and extract clean keywords for a database keyword search.
                Respond with JSON only, no markdown fences, no commentary, matching this shape:
                {"resultTypes": string[], "keywords": string}

                Valid resultTypes values (use only these; an empty array means search everything):
                ISSUE, POST, PROVIDER, EVENT, LOST_FOUND.
                "keywords" must be a short plain-text phrase with filler words like "find", "who",
                "show me" removed - never invent words not implied by the query.

                Query: "%s"
                """.formatted(query);

        RawSearchQueryInterpretation raw;
        try {
            raw = callWithTimeout(() -> chatClient.prompt().user(prompt).call().entity(RawSearchQueryInterpretation.class));
        } catch (TimeoutException ex) {
            throw new AIServiceException("Claude did not respond within " + aiProperties.timeoutMs() + "ms", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Claude search interpretation failed", ex);
        }

        if (raw == null) {
            throw new InvalidAIResponseException("Claude returned an empty search interpretation");
        }

        return new SearchQueryInterpretation(
                AIResponseValidator.safeResultTypes(raw.resultTypes()),
                AIResponseValidator.requireNonBlank(raw.keywords(), query),
                AIProvider.CLAUDE
        );
    }

    private ChatClient requireChatClient() {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new AIServiceException("Claude chat client is not configured");
        }
        return chatClient;
    }

    private <T> T callWithTimeout(Callable<T> call) throws Exception {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                return call.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        try {
            return future.get(aiProperties.timeoutMs(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException ex) {
            throw (ex.getCause() instanceof Exception e) ? e : ex;
        }
    }
}
