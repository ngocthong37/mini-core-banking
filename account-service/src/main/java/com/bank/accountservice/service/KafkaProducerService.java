package com.bank.accountservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "transaction-events";

    public void sendTransactionEvent(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
