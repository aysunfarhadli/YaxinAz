package com.yaxinaz.poll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {

    boolean existsByPollIdAndUserId(Long pollId, Long userId);

    Optional<PollVote> findByPollIdAndUserId(Long pollId, Long userId);

    long countByPollId(Long pollId);

    @Query("select v.option.id as optionId, count(v) as total from PollVote v where v.poll.id = :pollId group by v.option.id")
    List<PollOptionCount> countByPollIdGroupByOption(@Param("pollId") Long pollId);

    interface PollOptionCount {
        Long getOptionId();
        Long getTotal();
    }
}
