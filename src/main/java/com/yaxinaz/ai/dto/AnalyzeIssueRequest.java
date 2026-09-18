package com.yaxinaz.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnalyzeIssueRequest(
        @NotBlank @Size(max = 4000) String rawText,
        String buildingOrLocation,
        String language
) {
}
