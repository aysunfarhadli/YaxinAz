package com.yaxinaz.poll;

import com.yaxinaz.poll.dto.CreatePollRequest;
import com.yaxinaz.poll.dto.PollResponse;
import com.yaxinaz.poll.dto.VoteRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities/{communityId}/polls")
@RequiredArgsConstructor
@Tag(name = "Polls")
public class PollController {

    private final PollService pollService;

    @PostMapping
    public ResponseEntity<PollResponse> create(@PathVariable Long communityId, @Valid @RequestBody CreatePollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pollService.createPoll(communityId, request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PollResponse>> list(
            @PathVariable Long communityId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pollService.listPolls(communityId, pageable));
    }

    @GetMapping("/{pollId}")
    public ResponseEntity<PollResponse> get(@PathVariable Long communityId, @PathVariable Long pollId) {
        return ResponseEntity.ok(pollService.getPoll(communityId, pollId));
    }

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<PollResponse> vote(
            @PathVariable Long communityId, @PathVariable Long pollId, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(pollService.vote(communityId, pollId, request.optionId()));
    }
}
