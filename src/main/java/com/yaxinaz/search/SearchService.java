package com.yaxinaz.search;

import com.yaxinaz.ai.AIProvider;
import com.yaxinaz.ai.CommunityAIService;
import com.yaxinaz.ai.dto.SearchQueryInterpretation;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.event.CommunityEventRepository;
import com.yaxinaz.exception.RateLimitExceededException;
import com.yaxinaz.feed.PostRepository;
import com.yaxinaz.issue.IssueRepository;
import com.yaxinaz.lostfound.LostFoundRepository;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.search.dto.SearchResultItem;
import com.yaxinaz.search.dto.SearchResultsResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.security.ratelimit.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Plain Java keyword search (spec section 62) by default - every result comes from a real LIKE
 * query the way any other list endpoint in this app does. When {@code useAi} is set, a natural-
 * language query is first run through {@link CommunityAIService#interpretSearchQuery}, which only
 * narrows which result types to look at and cleans up the keyword phrase - it never returns search
 * results itself, so AI can narrow or mis-narrow a search but can never fabricate a result.
 */
@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_RESULTS_PER_TYPE = 5;
    private static final int SNIPPET_LENGTH = 140;
    private static final List<SearchResultItem> NONE = List.of();

    private final IssueRepository issueRepository;
    private final PostRepository postRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final CommunityEventRepository eventRepository;
    private final LostFoundRepository lostFoundRepository;
    private final CommunityService communityService;
    private final CommunityAIService communityAIService;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;

    /**
     * Not read-only: when {@code useAi} is set, {@link CommunityAIService#interpretSearchQuery}
     * goes through {@link com.yaxinaz.ai.CommunityAIServiceRouter}, which writes an
     * {@link com.yaxinaz.ai.AIUsageLog} row for every attempt - a read-only transaction here would
     * make Postgres reject that insert.
     */
    @Transactional
    public SearchResultsResponse search(String query, Long communityId, boolean useAi, String language) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.isEmpty()) {
            return new SearchResultsResponse(NONE, NONE, NONE, NONE, NONE, null);
        }

        String effectiveQuery = trimmed;
        Set<String> allowedTypes = null;
        AIProvider aiProvider = null;

        if (useAi) {
            Long userId = SecurityUtils.currentUserId();
            if (!rateLimiterService.tryConsume("ai:" + userId, rateLimitProperties.aiRequestsPerMinute())) {
                throw new RateLimitExceededException("Too many AI requests. Please wait a minute and try again.");
            }
            SearchQueryInterpretation interpretation = communityAIService.interpretSearchQuery(trimmed, language);
            if (interpretation.keywords() != null && !interpretation.keywords().isBlank()) {
                effectiveQuery = interpretation.keywords();
            }
            if (interpretation.resultTypes() != null && !interpretation.resultTypes().isEmpty()) {
                allowedTypes = new HashSet<>(interpretation.resultTypes());
            }
            aiProvider = interpretation.provider();
        }

        Pageable limit = PageRequest.of(0, MAX_RESULTS_PER_TYPE);

        List<SearchResultItem> providers = wants(allowedTypes, "PROVIDER")
                ? providerProfileRepository.searchByKeyword(effectiveQuery, limit).stream()
                        .map(p -> new SearchResultItem("PROVIDER", p.getId(), p.getBusinessName(), snippet(p.getBio())))
                        .toList()
                : NONE;

        if (communityId == null) {
            return new SearchResultsResponse(NONE, NONE, providers, NONE, NONE, aiProvider);
        }
        communityService.requireApprovedMember(SecurityUtils.currentUserId(), communityId);

        List<SearchResultItem> issues = wants(allowedTypes, "ISSUE")
                ? issueRepository.searchByKeyword(communityId, effectiveQuery, limit).stream()
                        .map(i -> new SearchResultItem("ISSUE", i.getId(), i.getTitle(), snippet(i.getDescription())))
                        .toList()
                : NONE;
        List<SearchResultItem> posts = wants(allowedTypes, "POST")
                ? postRepository.searchByKeyword(communityId, effectiveQuery, limit).stream()
                        .map(p -> new SearchResultItem("POST", p.getId(), p.getTitle() != null ? p.getTitle() : snippet(p.getContent()), snippet(p.getContent())))
                        .toList()
                : NONE;
        List<SearchResultItem> events = wants(allowedTypes, "EVENT")
                ? eventRepository.searchByKeyword(communityId, effectiveQuery, limit).stream()
                        .map(e -> new SearchResultItem("EVENT", e.getId(), e.getTitle(), snippet(e.getDescription())))
                        .toList()
                : NONE;
        List<SearchResultItem> lostFound = wants(allowedTypes, "LOST_FOUND")
                ? lostFoundRepository.searchByKeyword(communityId, effectiveQuery, limit).stream()
                        .map(l -> new SearchResultItem("LOST_FOUND", l.getId(), l.getTitle(), snippet(l.getDescription())))
                        .toList()
                : NONE;

        return new SearchResultsResponse(issues, posts, providers, events, lostFound, aiProvider);
    }

    private boolean wants(Set<String> allowedTypes, String type) {
        return allowedTypes == null || allowedTypes.contains(type);
    }

    private String snippet(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.length() > SNIPPET_LENGTH ? text.substring(0, SNIPPET_LENGTH) + "..." : text;
    }
}
