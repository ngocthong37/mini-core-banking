package com.bank.accountservice.service;

import com.bank.accountservice.dto.CreateAccountRequest;
import com.bank.accountservice.dto.TransactionRequest;
import com.bank.accountservice.dto.TransferRequest;
import com.bank.accountservice.dto.UserResponse;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.accountservice.repository.httpclient.UserClient;
import com.bank.common.event.TransactionEvent;
import com.bank.common.event.TransferEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;

    public Account createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setUserId(request.getUserId());
        account.setAccountType(request.getAccountType());
        account.setBalance(0.0);
        account.setIsActive(true);
        account.setCreatedAt(new Date());
        account.setAccountNumber("AC" + System.currentTimeMillis());
        return accountRepository.save(account);
    }

    public Account deposit(TransactionRequest request) {
        Account acc = accountRepository.findByAccountNumber(request.getAccountNumber()).orElseThrow();
        acc.setBalance(acc.getBalance() + request.getAmount());
        UserResponse userResponse = userClient.getUserProfile(acc.getUserId()).getResult();
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID().toString(), "DEPOSIT", request.getAccountNumber(), request.getAmount(), LocalDateTime.now(), userResponse.getUsername()
        );

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("transaction-events", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return accountRepository.save(acc);
    }

    public Account withdraw(TransactionRequest request) {
        Account acc = accountRepository.findByAccountNumber(request.getAccountNumber()).orElseThrow();
        if (acc.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }
        acc.setBalance(acc.getBalance() - request.getAmount());

        UserResponse userResponse = userClient.getUserProfile(acc.getUserId()).getResult();

        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID().toString(), "WITHDRAW", request.getAccountNumber(), request.getAmount(), LocalDateTime.now(),
                userResponse.getUsername()
        );

        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("transaction-events", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return accountRepository.save(acc);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        Account from = accountRepository.findByAccountNumber(request.getFromAccount()).orElseThrow();
        Account to = accountRepository.findByAccountNumber(request.getToAccount()).orElseThrow();
        if (from.getBalance() < request.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }
        from.setBalance(from.getBalance() - request.getAmount());
        to.setBalance(to.getBalance() + request.getAmount());
        accountRepository.save(from);
        accountRepository.save(to);
        handleTransfer(request);
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id).orElseThrow();
    }

    public List<Account> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByUserId(customerId);
    }

    public void handleTransfer(TransferRequest request) {
        TransferEvent event = new TransferEvent("TRANSFER", request.getFromAccount(), request.getToAccount(), request.getAmount(), new Date());
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("transfer-events", message);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
