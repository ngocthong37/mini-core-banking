package com.bank.accountservice.service;

import com.bank.accountservice.dto.TransactionRequest;
import com.bank.accountservice.dto.TransferRequest;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.repository.AccountRepository;
import com.bank.common.event.TransactionEvent;
import com.bank.common.event.TransferEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AccountService accountService;

    // ✅ DEPOSIT

    @Test
    void deposit_shouldIncreaseBalanceAndSendEvent() throws Exception {
        String accountNumber = "123456";
        double initial = 1000.0;
        double deposit = 5000.0;

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initial);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(objectMapper.writeValueAsString(any(TransactionEvent.class))).thenReturn("mock-json");

        TransactionRequest request = new TransactionRequest();
        request.setAmount(deposit);
        request.setAccountNumber(accountNumber);

        accountService.deposit(request);

        assertEquals(6000.0, account.getBalance());
        verify(accountRepository).save(account);
        verify(kafkaTemplate).send(eq("transaction-events"), eq("mock-json"));
    }

    @Test
    void deposit_shouldThrowExceptionIfAccountNotFound() {
        String accountNumber = "not-exist";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber(accountNumber);
        request.setAmount(100.0);

        assertThrows(RuntimeException.class, () -> accountService.deposit(request));
    }

    // ✅ WITHDRAW

    @Test
    void withdraw_shouldDecreaseBalanceAndSendEvent() throws Exception {
        String accountNumber = "123456";
        double initial = 1000.0;
        double withdrawAmount = 400.0;

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initial);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(objectMapper.writeValueAsString(any(TransactionEvent.class))).thenReturn("mock-json");

        TransactionRequest request = new TransactionRequest();
        request.setAmount(withdrawAmount);
        request.setAccountNumber(accountNumber);

        accountService.withdraw(request);

        assertEquals(600.0, account.getBalance());
        verify(accountRepository).save(account);
        verify(kafkaTemplate).send(eq("transaction-events"), eq("mock-json"));
    }

    @Test
    void withdraw_shouldThrowExceptionIfBalanceInsufficient() {
        String accountNumber = "123456";
        double initial = 300.0;
        double withdrawAmount = 500.0;

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initial);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        TransactionRequest request = new TransactionRequest();
        request.setAmount(withdrawAmount);
        request.setAccountNumber(accountNumber);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.withdraw(request));
        assertEquals("Insufficient balance", ex.getMessage());
    }

    @Test
    void withdraw_shouldThrowExceptionIfAccountNotFound() {
        when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.empty());

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("no-account");
        request.setAmount(100.0);

        assertThrows(RuntimeException.class, () -> accountService.withdraw(request));
    }

    // ✅ TRANSFER

    @Test
    void transfer_shouldMoveAmountAndSendTransferEvent() throws Exception {
        String fromAccountNumber = "111";
        String toAccountNumber = "222";
        double initialFrom = 1000.0;
        double initialTo = 500.0;
        double transferAmount = 300.0;

        Account fromAccount = new Account();
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setBalance(initialFrom);

        Account toAccount = new Account();
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setBalance(initialTo);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(toAccount));
        when(objectMapper.writeValueAsString(any(TransferEvent.class))).thenReturn("mock-transfer-json");

        TransferRequest request = new TransferRequest();
        request.setFromAccount(fromAccountNumber);
        request.setToAccount(toAccountNumber);
        request.setAmount(transferAmount);

        accountService.transfer(request);

        assertEquals(700.0, fromAccount.getBalance());
        assertEquals(800.0, toAccount.getBalance());
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
        verify(kafkaTemplate).send(eq("transfer-events"), eq("mock-transfer-json"));
    }

    @Test
    void transfer_shouldThrowExceptionIfBalanceInsufficient() {
        String fromAccountNumber = "111";
        String toAccountNumber = "222";
        double transferAmount = 300.0;

        Account fromAccount = new Account();
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setBalance(100.0); // insufficient

        Account toAccount = new Account();
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setBalance(500.0);

        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(toAccount));

        TransferRequest request = new TransferRequest();
        request.setFromAccount(fromAccountNumber);
        request.setToAccount(toAccountNumber);
        request.setAmount(transferAmount);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.transfer(request));
        assertEquals("Insufficient balance", ex.getMessage());
    }

    @Test
    void transfer_shouldThrowExceptionIfAccountNotFound() {
        when(accountRepository.findByAccountNumber("from")).thenReturn(Optional.empty());

        TransferRequest request = new TransferRequest();
        request.setFromAccount("from");
        request.setToAccount("to");
        request.setAmount(100.0);

        assertThrows(RuntimeException.class, () -> accountService.transfer(request));
    }
}
