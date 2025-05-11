package com.bank.accountservice.consumer;

import com.bank.accountservice.service.AuditLogService;
import com.bank.common.event.TransactionEvent;
import com.bank.common.event.TransferEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "transaction-events", groupId = "audit-service")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void transactionEvent(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            log.info("📥 Received transaction Event: {}", event);

            auditLogService.save(event.getType(), "system", message);

        } catch (Exception e) {
            log.error("❌ Failed to process transaction event", e);
        }
    }

    @KafkaListener(topics = "transfer-events", groupId = "audit-service")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void transferEvent(String message) throws JsonProcessingException {
        TransferEvent event = objectMapper.readValue(message, TransferEvent.class);
        log.info("📥 Received transfer Event: {}", event);
        auditLogService.save(event.getType(), "system", message);
    }

}
