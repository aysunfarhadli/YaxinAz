package com.yaxinaz.servicerequest;

import com.yaxinaz.audit.AuditActionType;
import com.yaxinaz.audit.AuditLogService;
import com.yaxinaz.exception.InvalidServiceRequestStateException;
import com.yaxinaz.exception.ServiceRequestNotFoundException;
import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.provider.ProviderProfile;
import com.yaxinaz.provider.ProviderService;
import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.servicerequest.dto.CreateServiceRequestRequest;
import com.yaxinaz.servicerequest.dto.ServiceRequestResponse;
import com.yaxinaz.servicerequest.event.ServiceRequestStatusChangedEvent;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private static final Logger log = LoggerFactory.getLogger(ServiceRequestService.class);

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ProviderService providerService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogService auditLogService;

    @Transactional
    public ServiceRequestResponse createRequest(CreateServiceRequestRequest request) {
        Long userId = SecurityUtils.currentUserId();
        User customer = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException("User not found"));
        ProviderProfile provider = providerService.getProviderOrThrow(request.providerId());

        ServiceRequest serviceRequest = serviceRequestRepository.save(ServiceRequest.builder()
                .customer(customer)
                .provider(provider)
                .category(request.category())
                .description(request.description().trim())
                .status(ServiceRequestStatus.REQUESTED)
                .build());

        log.info("SERVICE_REQUEST_CREATED id={} customerId={} providerId={}", serviceRequest.getId(), userId, provider.getId());
        return toResponse(serviceRequest);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceRequestResponse> myRequests(Pageable pageable) {
        Long userId = SecurityUtils.currentUserId();
        Page<ServiceRequest> page = serviceRequestRepository.findAllByCustomerId(userId, pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ServiceRequestResponse> incomingRequests(Pageable pageable) {
        ProviderProfile provider = requireOwnProviderProfile();
        Page<ServiceRequest> page = serviceRequestRepository.findAllByProviderId(provider.getId(), pageable);
        return PagedResponse.of(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public ServiceRequestResponse getRequest(Long id) {
        ServiceRequest serviceRequest = getRequestOrThrow(id);
        Long userId = SecurityUtils.currentUserId();
        boolean isCustomer = serviceRequest.getCustomer().getId().equals(userId);
        boolean isProvider = serviceRequest.getProvider().getUser().getId().equals(userId);
        if (!isCustomer && !isProvider) {
            throw new UnauthorizedResourceAccessException("You do not have access to this service request");
        }
        return toResponse(serviceRequest);
    }

    @Transactional
    public ServiceRequestResponse updateStatus(Long id, ServiceRequestStatus newStatus) {
        ServiceRequest serviceRequest = getRequestOrThrow(id);
        ServiceRequestStatus current = serviceRequest.getStatus();
        if (!ServiceRequestStatusTransitionPolicy.isAllowed(current, newStatus)) {
            throw new InvalidServiceRequestStateException(
                    "Cannot transition service request from " + current + " to " + newStatus);
        }

        Long userId = SecurityUtils.currentUserId();
        boolean isProviderActor = serviceRequest.getProvider().getUser().getId().equals(userId);
        boolean isCustomerActor = serviceRequest.getCustomer().getId().equals(userId);

        if (newStatus == ServiceRequestStatus.CANCELLED) {
            if (!isProviderActor && !isCustomerActor) {
                throw new UnauthorizedResourceAccessException("You do not have access to this service request");
            }
        } else if (!isProviderActor) {
            throw new UnauthorizedResourceAccessException("Only the assigned provider can perform this action");
        }

        serviceRequest.setStatus(newStatus);
        if (newStatus == ServiceRequestStatus.COMPLETED) {
            serviceRequest.setCompletedAt(Instant.now());
        }
        log.info("SERVICE_REQUEST_STATUS_CHANGED id={} from={} to={} actorId={}", id, current, newStatus, userId);
        auditLogService.record(userId, AuditActionType.SERVICE_REQUEST_STATUS_CHANGED, "ServiceRequest", id,
                current.name(), newStatus.name());
        eventPublisher.publishEvent(new ServiceRequestStatusChangedEvent(
                serviceRequest.getId(), serviceRequest.getCustomer().getId(), serviceRequest.getProvider().getId(),
                serviceRequest.getProvider().getBusinessName(), newStatus));
        return toResponse(serviceRequest);
    }

    public ServiceRequest getRequestOrThrow(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
    }

    private ProviderProfile requireOwnProviderProfile() {
        Long userId = SecurityUtils.currentUserId();
        return providerService.findOwnProfileOrThrow(userId);
    }

    private ServiceRequestResponse toResponse(ServiceRequest sr) {
        return new ServiceRequestResponse(
                sr.getId(), sr.getCustomer().getId(), sr.getCustomer().getFullName(),
                sr.getProvider().getId(), sr.getProvider().getBusinessName(), sr.getCategory(),
                sr.getDescription(), sr.getStatus(), sr.getCreatedAt(), sr.getUpdatedAt(), sr.getCompletedAt());
    }
}
