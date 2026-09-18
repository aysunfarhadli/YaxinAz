package com.yaxinaz.health;

import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.community.CommunityMembershipRepository;
import com.yaxinaz.community.MembershipStatus;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.health.dto.OperationsCenterResponse;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueRepository;
import com.yaxinaz.issue.IssueSpecifications;
import com.yaxinaz.issue.IssueStatus;
import com.yaxinaz.moderation.ModerationService;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.provider.ProviderSpecifications;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Composes existing services/repositories into one dashboard payload (spec section 110) - no query
 * logic is duplicated here that isn't already expressed as an IssueSpecifications/
 * ProviderSpecifications filter elsewhere.
 */
@Service
@RequiredArgsConstructor
public class OperationsCenterService {

    private static final List<IssueStatus> UNRESOLVED_STATUSES = List.of(
            IssueStatus.OPEN, IssueStatus.ACKNOWLEDGED, IssueStatus.IN_PROGRESS, IssueStatus.WAITING_FOR_VENDOR);

    private final IssueRepository issueRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ModerationService moderationService;
    private final SystemHealthService systemHealthService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public OperationsCenterResponse compute() {
        if (SecurityUtils.currentRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only platform admins can access the Operations Center");
        }

        long criticalIssues = issueRepository.count(Specification.allOf(
                IssueSpecifications.notDeleted(), IssueSpecifications.hasPriority(IssuePriority.CRITICAL),
                IssueSpecifications.statusIn(UNRESOLVED_STATUSES)));
        long staleIssues = issueRepository.count(Specification.allOf(
                IssueSpecifications.notDeleted(), IssueSpecifications.isStale(true)));
        long escalatedIssues = issueRepository.count(Specification.allOf(
                IssueSpecifications.notDeleted(), IssueSpecifications.isEscalated(true)));

        long pendingMemberships = membershipRepository.countByStatus(MembershipStatus.PENDING);
        long pendingProviders = providerProfileRepository.count(Specification.allOf(
                ProviderSpecifications.notDeleted(), ProviderSpecifications.isVerified(false)));
        long moderationQueue = moderationService.pendingCount();

        var recentAuditTrail = auditLogService.list(null, null, null, null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))).content();

        return new OperationsCenterResponse(
                criticalIssues, staleIssues, escalatedIssues, pendingMemberships, pendingProviders,
                moderationQueue, systemHealthService.check(), recentAuditTrail);
    }
}
