package com.yaxinaz.analytics;

import com.yaxinaz.analytics.dto.StatisticsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/api/communities/{id}/statistics")
    public ResponseEntity<StatisticsResponse> statistics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.computeStatistics(id));
    }
}
