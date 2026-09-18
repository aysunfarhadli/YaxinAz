package com.yaxinaz.health;

import com.yaxinaz.exception.UnauthorizedResourceAccessException;
import com.yaxinaz.health.dto.OperationsCenterResponse;
import com.yaxinaz.health.dto.SystemHealthResponse;
import com.yaxinaz.security.SecurityUtils;
import com.yaxinaz.user.Role;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "System Health / Operations Center")
public class SystemHealthController {

    private final SystemHealthService systemHealthService;
    private final OperationsCenterService operationsCenterService;

    @GetMapping("/system-health")
    public ResponseEntity<SystemHealthResponse> systemHealth() {
        if (SecurityUtils.currentRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedResourceAccessException("Only platform admins can view system health");
        }
        return ResponseEntity.ok(systemHealthService.check());
    }

    @GetMapping("/operations-center")
    public ResponseEntity<OperationsCenterResponse> operationsCenter() {
        return ResponseEntity.ok(operationsCenterService.compute());
    }
}
