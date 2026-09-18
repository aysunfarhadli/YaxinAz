package com.yaxinaz.servicerequest;

import com.yaxinaz.response.PagedResponse;
import com.yaxinaz.servicerequest.dto.CreateServiceRequestRequest;
import com.yaxinaz.servicerequest.dto.ServiceRequestResponse;
import com.yaxinaz.servicerequest.dto.UpdateServiceRequestStatusRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
@Tag(name = "Service Requests")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping
    public ResponseEntity<ServiceRequestResponse> create(@Valid @RequestBody CreateServiceRequestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceRequestService.createRequest(request));
    }

    @GetMapping("/my")
    public ResponseEntity<PagedResponse<ServiceRequestResponse>> myRequests(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.myRequests(pageable));
    }

    @GetMapping("/provider")
    public ResponseEntity<PagedResponse<ServiceRequestResponse>> incomingRequests(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(serviceRequestService.incomingRequests(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRequestResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(serviceRequestService.getRequest(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateServiceRequestStatusRequest request) {
        return ResponseEntity.ok(serviceRequestService.updateStatus(id, request.status()));
    }
}
