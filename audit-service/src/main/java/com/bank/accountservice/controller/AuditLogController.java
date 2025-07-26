package com.bank.accountservice.controller;

import com.bank.accountservice.entity.AuditLog;
import com.bank.accountservice.service.AuditLogService;
import com.bank.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<?> getLogs(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogPage = auditLogService.getLogsByUsername(username, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("content", auditLogPage.getContent());
        result.put("totalElements", auditLogPage.getTotalElements());
        result.put("totalPages", auditLogPage.getTotalPages());
        result.put("pageSize", auditLogPage.getSize());
        result.put("currentPage", auditLogPage.getNumber());

        return ApiResponse.builder()
                .code(1000)
                .result(result)
                .message("Success")
                .build();
    }

}
