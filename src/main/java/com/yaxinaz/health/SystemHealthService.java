package com.yaxinaz.health;

import com.yaxinaz.ai.CommunityAIServiceRouter;
import com.yaxinaz.config.properties.FeatureProperties;
import com.yaxinaz.health.dto.SystemHealthResponse;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;

/**
 * Custom admin-facing health view (spec sections 78-79), distinct from /actuator/health -
 * exposes what a Yaxin.az operator actually cares about (AI provider mode, WebSocket feature flag,
 * active Spring profile) without leaking config details a public health probe shouldn't reveal.
 */
@Service
@RequiredArgsConstructor
public class SystemHealthService {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthService.class);

    private final UserRepository userRepository;
    private final CommunityAIServiceRouter communityAIServiceRouter;
    private final FeatureProperties featureProperties;
    private final Environment environment;

    public SystemHealthResponse check() {
        String databaseStatus = checkDatabase();
        return new SystemHealthResponse(
                "ONLINE",
                databaseStatus,
                communityAIServiceRouter.currentMode(),
                featureProperties.websocketEnabled() ? "ACTIVE" : "DISABLED",
                activeProfile(),
                Instant.now()
        );
    }

    private String checkDatabase() {
        try {
            userRepository.count();
            return "ONLINE";
        } catch (Exception ex) {
            log.error("Database health check failed: {}", ex.getMessage());
            return "OFFLINE";
        }
    }

    private String activeProfile() {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length == 0 ? "default" : String.join(",", Arrays.asList(profiles));
    }
}
