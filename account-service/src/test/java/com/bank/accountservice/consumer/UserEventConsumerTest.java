package com.bank.accountservice.consumer;

import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.common.event.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UserEventConsumer consumer;

    @Test
    void handleUserCreatedEvent_shouldSaveAccount_whenValidMessage() throws Exception {
        // Arrange
        String userId = UUID.randomUUID().toString();
        String username = "john_doe";

        UserCreatedEvent event = new UserCreatedEvent();
        event.setId(userId);
        event.setUsername(username);
        event.setCreatedAt(new Date());

        String message = "{\"id\":\"" + userId + "\",\"username\":\"" + username + "\",\"createdAt\":\"" + event.getCreatedAt() + "\"}";

        when(objectMapper.readValue(message, UserCreatedEvent.class)).thenReturn(event);

        // Act
        consumer.handleUserCreatedEvent(message);

        // Assert
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        Account saved = accountCaptor.getValue();
        assertEquals(UUID.fromString(userId), saved.getUserId());
        assertEquals(0.0, saved.getBalance());
        assertEquals("SAVINGS", saved.getAccountType());
        assertTrue(saved.getIsActive());
    }

    @Test
    void handleUserCreatedEvent_shouldLogError_whenJsonParsingFails() throws Exception {
        // Arrange
        String invalidJson = "{invalid json}";

        when(objectMapper.readValue(invalidJson, UserCreatedEvent.class))
                .thenThrow(new RuntimeException("JSON parse error"));

        // Act
        consumer.handleUserCreatedEvent(invalidJson);

        verify(accountRepository, never()).save(any());
    }
}
