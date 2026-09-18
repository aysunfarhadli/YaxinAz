package com.yaxinaz.provider;

import com.yaxinaz.provider.dto.CreateProviderProfileRequest;
import com.yaxinaz.provider.dto.ProviderProfileResponse;
import com.yaxinaz.provider.dto.VerifyProviderRequest;
import com.yaxinaz.response.PagedResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Service Providers")
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ProviderProfileResponse> create(@Valid @RequestBody CreateProviderProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(providerService.createProfile(request));
    }

    @PatchMapping("/me")
    public ResponseEntity<ProviderProfileResponse> updateOwn(@Valid @RequestBody CreateProviderProfileRequest request) {
        return ResponseEntity.ok(providerService.updateOwnProfile(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProviderProfileResponse>> list(
            @RequestParam(required = false) ServiceCategory category,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Double minRating,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(providerService.listProviders(category, verified, minRating, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderProfileResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(providerService.getProvider(id));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ProviderProfileResponse> verify(@PathVariable Long id, @Valid @RequestBody VerifyProviderRequest request) {
        return ResponseEntity.ok(providerService.setVerified(id, request.verified()));
    }
}
