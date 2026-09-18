package com.yaxinaz.poll;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Spec section 45: expired polls stop accepting votes even if nobody opens the poll again. */
@Component
@RequiredArgsConstructor
public class PollMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PollMaintenanceScheduler.class);

    private final PollRepository pollRepository;

    @Scheduled(fixedRateString = "${yaxinaz.escalation.check-interval-ms:300000}")
    @Transactional
    public void deactivateExpiredPolls() {
        List<Poll> expired = pollRepository.findAllByActiveTrueAndExpiresAtBefore(Instant.now());
        for (Poll poll : expired) {
            poll.setActive(false);
            log.info("POLL_EXPIRED pollId={} communityId={}", poll.getId(), poll.getCommunity().getId());
        }
    }
}
