package com.bank.accountservice.consumer;

import com.bank.accountservice.repository.impl.EmailServiceRepositoryImp;
import com.bank.common.event.NotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final EmailServiceRepositoryImp emailServiceRepositoryImp;

    @KafkaListener(topics = "send-notification", groupId = "notification-service")
    public void handleUserCreatedEvent(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("Received event from Kafka: {}", event);
            String[] cc = {};
            emailServiceRepositoryImp.sendMail(event.getTo(), cc, event.getSubject(), null);

            log.info("Created account for user {}", event.getTo());
        } catch (Exception e) {
            log.error("Failed to process user-created event", e);
        }
    }
}
