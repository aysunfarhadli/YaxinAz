package com.yaxinaz.ai.dto;

import com.yaxinaz.ai.AIProvider;

/**
 * AI never posts anything itself - this is a draft the resident/admin can edit and must explicitly
 * submit via the normal {@code POST /api/communities/{id}/posts} endpoint (spec section 1: "AI
 * understands human language. Java controls the system.").
 */
public record AnnouncementDraftResponse(String title, String content, AIProvider provider) {
}
