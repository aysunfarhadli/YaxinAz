package com.yaxinaz.poll;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PollRepository extends JpaRepository<Poll, Long> {

    Optional<Poll> findByIdAndCommunityId(Long id, Long communityId);

    Page<Poll> findAllByCommunityId(Long communityId, Pageable pageable);

    List<Poll> findAllByActiveTrueAndExpiresAtBefore(Instant instant);
}
