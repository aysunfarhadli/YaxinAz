package com.yaxinaz.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DraftAnnouncementRequest(
        @NotBlank @Size(max = 2000) String rawNotes,
        String postType,
        String language
) {
}
