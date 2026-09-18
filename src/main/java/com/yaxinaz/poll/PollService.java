package com.yaxinaz.poll;

import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityService;
import com.yaxinaz.exception.AlreadyVotedException;
import com.yaxinaz.exception.PollExpiredException;
import com.yaxinaz.exception.PollNotFoundException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.poll.dto.CreatePollRequest;
import com.yaxinaz.poll.dto.PollOptionResponse;
import com.yaxinaz.poll.dto.PollResponse;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserRepository userRepository;
    private final CommunityService communityService;

    @Transactional
    public PollResponse createPoll(Long communityId, CreatePollRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Community community = communityService.getCommunityOrThrow(communityId);
        communityService.requireApprovedMember(userId, communityId);

        Poll poll = pollRepository.save(Poll.builder()
                .community(community)
                .question(request.question().trim())
                .createdBy(userId)
                .expiresAt(request.expiresAt())
                .active(true)
                .build());

        List<PollOption> options = request.options().stream()
                .map(text -> pollOptionRepository.save(PollOption.builder().poll(poll).optionText(text.trim()).build()))
                .toList();

        return toResponse(poll, options, userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PollResponse> listPolls(Long communityId, Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);

        Page<Poll> page = pollRepository.findAllByCommunityId(communityId, pageable);
        return PagedResponse.of(page, poll ->
                toResponse(poll, pollOptionRepository.findAllByPollIdOrderByIdAsc(poll.getId()), userId));
    }

    @Transactional(readOnly = true)
    public PollResponse getPoll(Long communityId, Long pollId) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);
        Poll poll = getPollOrThrow(communityId, pollId);
        return toResponse(poll, pollOptionRepository.findAllByPollIdOrderByIdAsc(poll.getId()), userId);
    }

    @Transactional
    public PollResponse vote(Long communityId, Long pollId, Long optionId) {
        Long userId = SecurityUtils.currentUserId();
        communityService.requireApprovedMember(userId, communityId);
        Poll poll = getPollOrThrow(communityId, pollId);

        if (!poll.isActive() || (poll.getExpiresAt() != null && poll.getExpiresAt().isBefore(Instant.now()))) {
            throw new PollExpiredException("This poll has expired and no longer accepts votes");
        }
        if (pollVoteRepository.existsByPollIdAndUserId(pollId, userId)) {
            throw new AlreadyVotedException("You have already voted in this poll");
        }

        PollOption option = pollOptionRepository.findByIdAndPollId(optionId, pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));

        pollVoteRepository.save(PollVote.builder().poll(poll).option(option).user(user).build());

        return toResponse(poll, pollOptionRepository.findAllByPollIdOrderByIdAsc(pollId), userId);
    }

    private Poll getPollOrThrow(Long communityId, Long pollId) {
        return pollRepository.findByIdAndCommunityId(pollId, communityId)
                .orElseThrow(() -> new PollNotFoundException(pollId));
    }

    private PollResponse toResponse(Poll poll, List<PollOption> options, Long currentUserId) {
        Map<Long, Long> voteCounts = pollVoteRepository.countByPollIdGroupByOption(poll.getId()).stream()
                .collect(Collectors.toMap(PollVoteRepository.PollOptionCount::getOptionId, PollVoteRepository.PollOptionCount::getTotal));
        long totalVotes = voteCounts.values().stream().mapToLong(Long::longValue).sum();

        List<PollOptionResponse> optionResponses = options.stream()
                .map(option -> {
                    long count = voteCounts.getOrDefault(option.getId(), 0L);
                    double percentage = totalVotes == 0 ? 0.0 : (count * 100.0) / totalVotes;
                    return new PollOptionResponse(option.getId(), option.getOptionText(), count, percentage);
                })
                .toList();

        var myVote = pollVoteRepository.findByPollIdAndUserId(poll.getId(), currentUserId);
        boolean voted = myVote.isPresent();
        Long myOptionId = myVote.map(v -> v.getOption().getId()).orElse(null);

        return new PollResponse(poll.getId(), poll.getCommunity().getId(), poll.getQuestion(), optionResponses,
                totalVotes, poll.getExpiresAt(), poll.isActive(), voted, myOptionId, poll.getCreatedAt());
    }
}
