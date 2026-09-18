package com.yaxinaz.search;

import com.yaxinaz.search.dto.SearchResultsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/api/search")
    public ResponseEntity<SearchResultsResponse> search(
            @RequestParam String q,
            @RequestParam(required = false) Long communityId,
            @RequestParam(defaultValue = "false") boolean useAi,
            @RequestParam(required = false) String lang) {
        return ResponseEntity.ok(searchService.search(q, communityId, useAi, lang));
    }
}
