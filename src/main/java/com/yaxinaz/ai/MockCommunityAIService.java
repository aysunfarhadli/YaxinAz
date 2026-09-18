package com.yaxinaz.ai;

import com.yaxinaz.ai.dto.AnnouncementDraftResponse;
import com.yaxinaz.ai.dto.IssueAIAnalysisResponse;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.analytics.dto.StatisticsResponse;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Deterministic, network-free stand-in for Claude. Used whenever AI is disabled, the API key is
 * missing, or Claude fails and mock fallback is enabled (spec sections 36-37). Keyword-based rather
 * than a real model - good enough for demo/course purposes, and fully reproducible for tests.
 */
@Service
public class MockCommunityAIService implements CommunityAIService {

    private static final Map<String, IssueCategory> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, String> AFFECTED_GROUP_KEYWORDS = new LinkedHashMap<>();
    private static final List<String> HIGH_URGENCY_KEYWORDS =
            List.of("urgent", "emergency", "danger", "unsafe", "cannot", "can't", "stopped working", "again");
    private static final List<String> CRITICAL_URGENCY_KEYWORDS =
            List.of("fire", "gas leak", "smoke", "collapse", "life-threatening", "no water", "no electricity");
    private static final Map<String, String> SEARCH_TYPE_KEYWORDS = new LinkedHashMap<>();
    private static final List<String> SEARCH_STOPWORD_PHRASES = List.of(
            "find ", "search for ", "show me ", "look for ", "looking for ", "is there ", "are there ", "any ");

    static {
        CATEGORY_KEYWORDS.put("elevator", IssueCategory.ELEVATOR);
        CATEGORY_KEYWORDS.put("lift", IssueCategory.ELEVATOR);
        CATEGORY_KEYWORDS.put("water", IssueCategory.WATER);
        CATEGORY_KEYWORDS.put("pipe", IssueCategory.WATER);
        CATEGORY_KEYWORDS.put("leak", IssueCategory.WATER);
        CATEGORY_KEYWORDS.put("electric", IssueCategory.ELECTRICITY);
        CATEGORY_KEYWORDS.put("power outage", IssueCategory.ELECTRICITY);
        CATEGORY_KEYWORDS.put("gas", IssueCategory.GAS);
        CATEGORY_KEYWORDS.put("heat", IssueCategory.HEATING);
        CATEGORY_KEYWORDS.put("radiator", IssueCategory.HEATING);
        CATEGORY_KEYWORDS.put("parking", IssueCategory.PARKING);
        CATEGORY_KEYWORDS.put("garage", IssueCategory.PARKING);
        CATEGORY_KEYWORDS.put("security", IssueCategory.SECURITY);
        CATEGORY_KEYWORDS.put("camera", IssueCategory.SECURITY);
        CATEGORY_KEYWORDS.put("intruder", IssueCategory.SECURITY);
        CATEGORY_KEYWORDS.put("noise", IssueCategory.NOISE);
        CATEGORY_KEYWORDS.put("loud", IssueCategory.NOISE);
        CATEGORY_KEYWORDS.put("clean", IssueCategory.CLEANING);
        CATEGORY_KEYWORDS.put("trash", IssueCategory.WASTE);
        CATEGORY_KEYWORDS.put("garbage", IssueCategory.WASTE);
        CATEGORY_KEYWORDS.put("waste", IssueCategory.WASTE);
        CATEGORY_KEYWORDS.put("internet", IssueCategory.INTERNET);
        CATEGORY_KEYWORDS.put("wifi", IssueCategory.INTERNET);
        CATEGORY_KEYWORDS.put("crack", IssueCategory.BUILDING_DAMAGE);
        CATEGORY_KEYWORDS.put("damage", IssueCategory.BUILDING_DAMAGE);
        CATEGORY_KEYWORDS.put("ceiling", IssueCategory.BUILDING_DAMAGE);
        CATEGORY_KEYWORDS.put("road", IssueCategory.ROAD);
        CATEGORY_KEYWORDS.put("pothole", IssueCategory.ROAD);
        CATEGORY_KEYWORDS.put("light", IssueCategory.LIGHTING);
        CATEGORY_KEYWORDS.put("lamp", IssueCategory.LIGHTING);
        CATEGORY_KEYWORDS.put("dog", IssueCategory.ANIMAL);
        CATEGORY_KEYWORDS.put("cat", IssueCategory.ANIMAL);
        CATEGORY_KEYWORDS.put("stray", IssueCategory.ANIMAL);
        CATEGORY_KEYWORDS.put("wheelchair", IssueCategory.ACCESSIBILITY);
        CATEGORY_KEYWORDS.put("ramp", IssueCategory.ACCESSIBILITY);

        AFFECTED_GROUP_KEYWORDS.put("elderly", "ELDERLY");
        AFFECTED_GROUP_KEYWORDS.put("senior", "ELDERLY");
        AFFECTED_GROUP_KEYWORDS.put("child", "CHILDREN");
        AFFECTED_GROUP_KEYWORDS.put("kids", "CHILDREN");
        AFFECTED_GROUP_KEYWORDS.put("disab", "DISABLED");
        AFFECTED_GROUP_KEYWORDS.put("wheelchair", "DISABLED");
        AFFECTED_GROUP_KEYWORDS.put("pet", "PET_OWNERS");

        SEARCH_TYPE_KEYWORDS.put("issue", "ISSUE");
        SEARCH_TYPE_KEYWORDS.put("problem", "ISSUE");
        SEARCH_TYPE_KEYWORDS.put("report", "ISSUE");
        SEARCH_TYPE_KEYWORDS.put("broken", "ISSUE");
        SEARCH_TYPE_KEYWORDS.put("complaint", "ISSUE");
        SEARCH_TYPE_KEYWORDS.put("post", "POST");
        SEARCH_TYPE_KEYWORDS.put("announcement", "POST");
        SEARCH_TYPE_KEYWORDS.put("discussion", "POST");
        SEARCH_TYPE_KEYWORDS.put("feed", "POST");
        SEARCH_TYPE_KEYWORDS.put("provider", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("service", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("plumber", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("electrician", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("clean", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("repair", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("hire", "PROVIDER");
        SEARCH_TYPE_KEYWORDS.put("event", "EVENT");
        SEARCH_TYPE_KEYWORDS.put("meeting", "EVENT");
        SEARCH_TYPE_KEYWORDS.put("lost", "LOST_FOUND");
        SEARCH_TYPE_KEYWORDS.put("found", "LOST_FOUND");
        SEARCH_TYPE_KEYWORDS.put("missing", "LOST_FOUND");
    }

    @Override
    public IssueAIAnalysisResponse analyzeIssue(String rawText, String buildingOrLocation, String language) {
        String lower = rawText.toLowerCase(Locale.ROOT);

        IssueCategory category = CATEGORY_KEYWORDS.entrySet().stream()
                .filter(entry -> lower.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(IssueCategory.OTHER);

        IssuePriority priority = IssuePriority.MEDIUM;
        if (CRITICAL_URGENCY_KEYWORDS.stream().anyMatch(lower::contains)) {
            priority = IssuePriority.CRITICAL;
        } else if (HIGH_URGENCY_KEYWORDS.stream().anyMatch(lower::contains)) {
            priority = IssuePriority.HIGH;
        }

        List<String> affectedGroups = new ArrayList<>();
        AFFECTED_GROUP_KEYWORDS.forEach((keyword, group) -> {
            if (lower.contains(keyword) && !affectedGroups.contains(group)) {
                affectedGroups.add(group);
            }
        });
        if (affectedGroups.isEmpty()) {
            affectedGroups.add("ALL_RESIDENTS");
        }

        List<String> tags = new ArrayList<>();
        tags.add(category.name().toLowerCase(Locale.ROOT));
        if (buildingOrLocation != null && !buildingOrLocation.isBlank()) {
            tags.add(buildingOrLocation.toLowerCase(Locale.ROOT).replace(" ", "-"));
        }

        String title = buildTitle(category, buildingOrLocation);
        String summary = buildSummary(rawText, category, priority);

        return new IssueAIAnalysisResponse(title, category, priority, summary, affectedGroups, tags, AIProvider.MOCK);
    }

    private String buildTitle(IssueCategory category, String buildingOrLocation) {
        String base = switch (category) {
            case ELEVATOR -> "Elevator issue";
            case WATER -> "Water supply issue";
            case ELECTRICITY -> "Electricity issue";
            case GAS -> "Gas supply issue";
            case HEATING -> "Heating issue";
            case PARKING -> "Parking issue";
            case SECURITY -> "Security concern";
            case NOISE -> "Noise complaint";
            case CLEANING -> "Cleaning issue";
            case WASTE -> "Waste collection issue";
            case INTERNET -> "Internet connectivity issue";
            case BUILDING_DAMAGE -> "Building damage reported";
            case ROAD -> "Road condition issue";
            case LIGHTING -> "Lighting issue";
            case ANIMAL -> "Animal-related report";
            case ACCESSIBILITY -> "Accessibility concern";
            case OTHER -> "Community issue reported";
        };
        return (buildingOrLocation != null && !buildingOrLocation.isBlank())
                ? base + " - " + buildingOrLocation
                : base;
    }

    private String buildSummary(String rawText, IssueCategory category, IssuePriority priority) {
        String trimmed = rawText.length() > 220 ? rawText.substring(0, 220) + "..." : rawText;
        return "AI summary (" + priority + " priority, " + category + "): " + trimmed;
    }

    @Override
    public AnnouncementDraftResponse draftAnnouncement(String rawNotes, String postType, String language) {
        String cleaned = rawNotes.trim().replaceAll("\\s+", " ");
        String capitalized = capitalize(cleaned);
        String content = capitalized.endsWith(".") || capitalized.endsWith("!") || capitalized.endsWith("?")
                ? capitalized : capitalized + ".";

        String[] words = cleaned.split(" ");
        String titleBase = String.join(" ", Arrays.copyOfRange(words, 0, Math.min(8, words.length)));
        String prefix = switch (postType == null ? "" : postType.toUpperCase(Locale.ROOT)) {
            case "ALERT" -> "Alert: ";
            case "ANNOUNCEMENT" -> "Announcement: ";
            case "EVENT" -> "Event: ";
            default -> "";
        };
        String title = prefix + capitalize(titleBase) + (words.length > 8 ? "..." : "");

        return new AnnouncementDraftResponse(title, content, AIProvider.MOCK);
    }

    @Override
    public String summarizeCommunity(StatisticsResponse stats, String communityName, String language) {
        if (stats.totalIssues() == 0) {
            return "No issues have been reported in " + communityName + " yet.";
        }
        String categoryPhrase = stats.mostCommonCategory() != null
                ? stats.mostCommonCategory().name().toLowerCase(Locale.ROOT).replace('_', ' ') + " problems were the most common"
                : "no single category dominated";
        return "This week residents of " + communityName + " reported " + stats.totalIssues() + " issue"
                + (stats.totalIssues() == 1 ? "" : "s") + ". " + capitalize(categoryPhrase) + ". "
                + stats.resolvedIssues() + " issue" + (stats.resolvedIssues() == 1 ? " was" : "s were") + " resolved, while "
                + stats.criticalIssues() + " critical issue" + (stats.criticalIssues() == 1 ? "" : "s") + " remain open.";
    }

    @Override
    public List<String> generateInsights(StatisticsResponse stats, String language) {
        List<String> insights = new ArrayList<>();
        if (stats.totalIssues() < 3) {
            return insights;
        }
        if (stats.escalatedIssues() > 0) {
            insights.add(stats.escalatedIssues() + " issue" + (stats.escalatedIssues() == 1 ? " has" : "s have")
                    + " been escalated and may need immediate admin attention.");
        }
        if (stats.resolutionRatePercent() >= 70) {
            insights.add("The community is resolving issues efficiently, with a "
                    + stats.resolutionRatePercent() + "% resolution rate.");
        } else if (stats.resolutionRatePercent() < 30) {
            insights.add("Resolution rate is low at " + stats.resolutionRatePercent()
                    + "%, which may indicate a backlog of unresolved issues.");
        }
        if (stats.mostCommonCategory() != null) {
            long topCount = stats.issuesByCategory().getOrDefault(stats.mostCommonCategory(), 0L);
            if (topCount >= stats.totalIssues() / 2.0 && stats.totalIssues() >= 4) {
                insights.add(stats.mostCommonCategory().name().toLowerCase(Locale.ROOT).replace('_', ' ')
                        + " issues make up more than half of all reports this period.");
            }
        }
        return insights;
    }

    @Override
    public SearchQueryInterpretation interpretSearchQuery(String query, String language) {
        String lower = query.toLowerCase(Locale.ROOT);

        List<String> types = new ArrayList<>();
        SEARCH_TYPE_KEYWORDS.forEach((keyword, type) -> {
            if (lower.contains(keyword) && !types.contains(type)) {
                types.add(type);
            }
        });

        String cleaned = lower;
        for (String phrase : SEARCH_STOPWORD_PHRASES) {
            cleaned = cleaned.replace(phrase, "");
        }
        cleaned = cleaned.trim();
        String keywords = cleaned.isEmpty() ? query.trim() : cleaned;

        return new SearchQueryInterpretation(types, keywords, AIProvider.MOCK);
    }

    private String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
