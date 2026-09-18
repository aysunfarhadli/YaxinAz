package com.yaxinaz.servicerequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    Optional<ServiceRequest> findByIdAndCustomerId(Long id, Long customerId);

    Optional<ServiceRequest> findByIdAndProviderId(Long id, Long providerId);

    Page<ServiceRequest> findAllByCustomerId(Long customerId, Pageable pageable);

    Page<ServiceRequest> findAllByProviderId(Long providerId, Pageable pageable);

    Page<ServiceRequest> findAllByProviderIdAndStatus(Long providerId, ServiceRequestStatus status, Pageable pageable);
}
