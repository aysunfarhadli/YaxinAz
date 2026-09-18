package com.yaxinaz.ai.dto;

import com.yaxinaz.ai.AIProvider;

import java.util.List;

/**
 * What the AI decided about a natural-language search query: which result types are actually
 * relevant (empty = search everything, same as no AI) and a cleaned-up keyword phrase. AI never
 * returns search results itself - {@link com.yaxinaz.search.SearchService} still runs the real
 * database LIKE queries against {@code keywords}, so results can never be fabricated.
 */
public record SearchQueryInterpretation(
        List<String> resultTypes,
        String keywords,
        AIProvider provider
) {
}
