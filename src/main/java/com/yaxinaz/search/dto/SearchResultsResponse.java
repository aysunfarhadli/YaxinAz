package com.yaxinaz.search.dto;

import com.yaxinaz.ai.AIProvider;

import java.util.List;

public record SearchResultsResponse(
        List<SearchResultItem> issues,
        List<SearchResultItem> posts,
        List<SearchResultItem> providers,
        List<SearchResultItem> events,
        List<SearchResultItem> lostFoundItems,
        AIProvider aiProvider
) {
}
