package com.yaxinaz.lostfound;

import com.yaxinaz.lostfound.dto.CreateLostFoundItemRequest;
import com.yaxinaz.lostfound.dto.LostFoundItemResponse;
import com.yaxinaz.lostfound.dto.UpdateLostFoundStatusRequest;
import com.yaxinaz.response.PagedResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities/{communityId}/lost-found")
@RequiredArgsConstructor
@Tag(name = "Lost & Found")
public class LostFoundController {

    private final LostFoundService lostFoundService;

    @PostMapping
    public ResponseEntity<LostFoundItemResponse> create(
            @PathVariable Long communityId, @Valid @RequestBody CreateLostFoundItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lostFoundService.createItem(communityId, request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<LostFoundItemResponse>> list(
            @PathVariable Long communityId,
            @RequestParam(required = false) LostFoundType type,
            @RequestParam(required = false) LostFoundStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(lostFoundService.listItems(communityId, type, status, pageable));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<LostFoundItemResponse> get(@PathVariable Long communityId, @PathVariable Long itemId) {
        return ResponseEntity.ok(lostFoundService.getItem(communityId, itemId));
    }

    @PatchMapping("/{itemId}/status")
    public ResponseEntity<LostFoundItemResponse> updateStatus(
            @PathVariable Long communityId, @PathVariable Long itemId, @Valid @RequestBody UpdateLostFoundStatusRequest request) {
        return ResponseEntity.ok(lostFoundService.updateStatus(communityId, itemId, request.status()));
    }
}
