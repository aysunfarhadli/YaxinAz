package com.yaxinaz.community;

import com.yaxinaz.community.dto.CommunityMemberResponse;
import com.yaxinaz.community.dto.CommunityResponse;
import com.yaxinaz.community.dto.CreateCommunityRequest;
import com.yaxinaz.community.dto.UpdateMembershipStatusRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Communities")
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<CommunityResponse> create(@Valid @RequestBody CreateCommunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.createCommunity(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CommunityResponse>> list(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(communityService.listCommunities(city, pageable));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<CommunityResponse>> mine() {
        return ResponseEntity.ok(communityService.listMyCommunities());
    }

    @GetMapping("/{communityId}")
    public ResponseEntity<CommunityResponse> get(@PathVariable Long communityId) {
        return ResponseEntity.ok(communityService.getCommunity(communityId));
    }

    @PostMapping("/{communityId}/join")
    public ResponseEntity<CommunityMemberResponse> join(@PathVariable Long communityId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communityService.requestToJoin(communityId));
    }

    @GetMapping("/{communityId}/members")
    public ResponseEntity<PagedResponse<CommunityMemberResponse>> members(
            @PathVariable Long communityId,
            @RequestParam(required = false) MembershipStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(communityService.listMembers(communityId, status, pageable));
    }

    @PatchMapping("/{communityId}/members/{membershipId}")
    public ResponseEntity<CommunityMemberResponse> reviewMembership(
            @PathVariable Long communityId,
            @PathVariable Long membershipId,
            @Valid @RequestBody UpdateMembershipStatusRequest request) {
        return ResponseEntity.ok(communityService.reviewMembership(communityId, membershipId, request.status()));
    }
}
