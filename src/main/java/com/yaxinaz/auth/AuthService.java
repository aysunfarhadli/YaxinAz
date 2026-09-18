package com.yaxinaz.auth;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.auth.dto.AuthResponse;
import com.yaxinaz.auth.dto.LoginRequest;
import com.yaxinaz.auth.dto.RegisterRequest;
import com.yaxinaz.config.properties.RateLimitProperties;
import com.yaxinaz.exception.EmailAlreadyExistsException;
import com.yaxinaz.exception.RateLimitExceededException;
import com.yaxinaz.exception.UserNotFoundException;
import com.yaxinaz.security.JwtService;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.security.UserPrincipal;
import com.yaxinaz.security.ratelimit.RateLimiterService;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserMapper;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RateLimiterService rateLimiterService;
    private final RateLimitProperties rateLimitProperties;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailAndDeletedFalse(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.RESIDENT)
                .preferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage() : "EN")
                .enabled(true)
                .build();
        user = userRepository.save(user);
        log.info("USER_REGISTERED userId={} email={}", user.getId(), user.getEmail());
        auditLogService.record(user.getId(), AuditActionType.USER_REGISTERED, "User", user.getId(), null, user.getEmail());

        return issueToken(new UserPrincipal(user), user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (!rateLimiterService.tryConsume("login:" + normalizedEmail, rateLimitProperties.loginAttemptsPerMinute())) {
            log.warn("LOGIN_RATE_LIMITED email={}", normalizedEmail);
            throw new RateLimitExceededException("Too many login attempts. Please wait a minute and try again.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        } catch (BadCredentialsException ex) {
            log.warn("LOGIN_FAILURE email={}", normalizedEmail);
            auditLogService.record(null, AuditActionType.LOGIN_FAILURE, "User", null, null, normalizedEmail);
            throw ex;
        }

        User user = userRepository.findByEmailAndDeletedFalse(normalizedEmail)
                .orElseThrow(() -> UserNotFoundException.byEmail(normalizedEmail));
        log.info("LOGIN_SUCCESS userId={} email={}", user.getId(), user.getEmail());
        auditLogService.record(user.getId(), AuditActionType.LOGIN_SUCCESS, "User", user.getId(), null, null);

        return issueToken(new UserPrincipal(user), user);
    }

    @Transactional(readOnly = true)
    public com.yaxinaz.user.dto.UserResponse me() {
        User user = userRepository.findByIdAndDeletedFalse(SecurityUtils.currentUserId())
                .orElseThrow(() -> UserNotFoundException.byId(SecurityUtils.currentUserId()));
        return userMapper.toResponse(user);
    }

    private AuthResponse issueToken(UserPrincipal principal, User user) {
        String token = jwtService.generateToken(principal);
        return AuthResponse.bearer(token, jwtService.extractExpiration(token), userMapper.toResponse(user));
    }
}
