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
import com.bank.common.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Mock
    private UserClient userClient;

    // ✅ DEPOSIT

    @Test
    void deposit_shouldIncreaseBalanceAndSendEvent() throws Exception {
        String accountNumber = "123456";
        double initial = 1000.0;
        double deposit = 5000.0;
        UUID userId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initial);
        account.setUserId(userId);

        TransactionRequest request = new TransactionRequest();
        request.setAmount(deposit);
        request.setAccountNumber(accountNumber);

        // Mock repository
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // Mock UserClient
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("mock-user");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);

        when(userClient.getUserProfile(userId)).thenReturn(apiResponse);

        // Mock JSON
        when(objectMapper.writeValueAsString(any(TransactionEvent.class))).thenReturn("mock-json");

        // Run
        accountService.deposit(request);

        // Assert balance
        assertEquals(6000.0, account.getBalance());

        // Verify calls
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
        UUID userId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(initial);
        account.setUserId(userId);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(objectMapper.writeValueAsString(any(TransactionEvent.class))).thenReturn("mock-json");

        TransactionRequest request = new TransactionRequest();
        request.setAmount(withdrawAmount);
        request.setAccountNumber(accountNumber);

        // Mock UserClient
        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("mock-user");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);

        when(userClient.getUserProfile(userId)).thenReturn(apiResponse);

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

    @Test
    void get_account_shouldReturnAccount() {
        Long accountId = 1L;

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(100.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = accountService.getAccount(accountId);

        // assert
        assertEquals(accountId, result.getId());
        assertEquals(account.getBalance(), result.getBalance());

        //
        verify(accountRepository).findById(accountId);
    }

    @Test
    void get_account_with_customer_id_shouldReturnAccount() {
        UUID uuid = UUID.randomUUID();

        Account account = new Account();
        account.setUserId(uuid);
        account.setBalance(100.0);

        List<Account> accounts = List.of(account);
        when(accountRepository.findByUserId(uuid)).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByCustomer(uuid);

        // assert
        assertEquals(result.size(), 1);
        assertEquals(accounts.get(0).getBalance(), result.get(0).getBalance());

        //
        verify(accountRepository).findByUserId(uuid);
    }

    @Test
    void test_create_Account() {
        UUID userId = UUID.randomUUID();
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUserId(userId);
        request.setAccountType("Saving");

        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUserId(userId);
        savedAccount.setBalance(0.0);
        savedAccount.setIsActive(true);
        savedAccount.setAccountNumber("AC123456");

        //
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        Account result = accountService.createAccount(request);

        assertEquals(savedAccount.getId(), result.getId());
        assertEquals(savedAccount.getBalance(), result.getBalance());

        // verify
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void withdraw_shouldThrowRuntimeException_whenKafkaFails() throws Exception {
        String accountNumber = "123456";
        double balance = 1000.0;
        double withdraw = 200.0;
        UUID userId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setUserId(userId);

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber(accountNumber);
        request.setAmount(withdraw);

        // Mock dependencies
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("test-user");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);

        when(userClient.getUserProfile(userId)).thenReturn(apiResponse);

        when(objectMapper.writeValueAsString(any(TransactionEvent.class)))
                .thenThrow(new JsonProcessingException("JSON error") {});

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.withdraw(request);
        });

        assertTrue(ex.getMessage().contains("JSON error"));

        verify(kafkaTemplate, never()).send(any(), any());

        verify(accountRepository, never()).save(account);
    }

    @Test
    void transfer_shouldThrowRuntimeException_whenKafkaFails() throws Exception {
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

        TransferRequest request = new TransferRequest();
        request.setFromAccount(fromAccountNumber);
        request.setToAccount(toAccountNumber);
        request.setAmount(transferAmount);

        // Mock dependencies
        when(accountRepository.findByAccountNumber(fromAccountNumber)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findByAccountNumber(toAccountNumber)).thenReturn(Optional.of(toAccount));

        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("test-user");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);

        when(objectMapper.writeValueAsString(any(TransferEvent.class)))
                .thenThrow(new JsonProcessingException("JSON error") {});

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.transfer(request);
        });

        assertTrue(ex.getMessage().contains("JSON error"));

        verify(kafkaTemplate, never()).send(any(), any());
    }

    @Test
    void deposit_shouldThrowRuntimeException_whenKafkaFails() throws Exception {
        String accountNumber = "123456";
        double balance = 1000.0;
        double withdraw = 200.0;
        UUID userId = UUID.randomUUID();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setUserId(userId);

        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber(accountNumber);
        request.setAmount(withdraw);

        // Mock dependencies
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        UserResponse userResponse = new UserResponse();
        userResponse.setUsername("test-user");

        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userResponse);

        when(userClient.getUserProfile(userId)).thenReturn(apiResponse);

        when(objectMapper.writeValueAsString(any(TransactionEvent.class)))
                .thenThrow(new JsonProcessingException("JSON error") {});

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            accountService.deposit(request);
        });

        assertTrue(ex.getMessage().contains("JSON error"));

        verify(kafkaTemplate, never()).send(any(), any());

        verify(accountRepository, never()).save(account);
    }

}
