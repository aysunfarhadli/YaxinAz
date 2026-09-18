package com.yaxinaz.search.dto;

public record SearchResultItem(
        String type,
        Long id,
        String title,
        String snippet
) {
}
