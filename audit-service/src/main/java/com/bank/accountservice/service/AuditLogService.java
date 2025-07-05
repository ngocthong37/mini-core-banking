package com.bank.accountservice.service;


import com.bank.accountservice.entity.AuditLog;
import com.bank.accountservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public void save(String action, String username, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .username(username)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        repository.save(log);
    }

    public Page<AuditLog> getLogsByUsername(String username, Pageable pageable) {
        return repository.findAllByUsername(username, pageable);
    }
}
