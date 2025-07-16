package com.bank.accountservice.controller;

import com.bank.accountservice.dto.CreateAccountRequest;
import com.bank.accountservice.dto.TransactionRequest;
import com.bank.accountservice.dto.TransferRequest;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;
    UUID uuid = UUID.randomUUID();

    @Test
    void testCreateAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(uuid, "SAVINGS");

        Account account = new Account();
        account.setId(1L);
        account.setUserId(uuid);
        account.setAccountNumber("123456789");
        account.setBalance(0.0);
        account.setAccountType("SAVINGS");
        account.setIsActive(true);
        account.setCreatedAt(new Date());

        Mockito.when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(account);

        mockMvc.perform(post("/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(uuid.toString()))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void testGetAccountById() throws Exception {
        Account account = new Account();
        account.setId(1L);
        account.setUserId(uuid);
        account.setAccountNumber("987654321");
        account.setBalance(5000.0);
        account.setAccountType("CURRENT");
        account.setIsActive(true);
        account.setCreatedAt(new Date());

        Mockito.when(accountService.getAccount(1L)).thenReturn(account);

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(5000.0))
                .andExpect(jsonPath("$.accountNumber").value("987654321"))
                .andExpect(jsonPath("$.accountType").value("CURRENT"));
    }

    @Test
    void testGetAccountsByCustomer() throws Exception {
        Account acc = new Account();
        acc.setId(1L);
        acc.setUserId(uuid);
        acc.setAccountNumber("ACC001");
        acc.setBalance(1000.0);
        acc.setAccountType("SAVINGS");
        acc.setIsActive(true);
        acc.setCreatedAt(new Date());

        List<Account> accounts = List.of(acc);
        Mockito.when(accountService.getAccountsByCustomer(uuid)).thenReturn(accounts);

        mockMvc.perform(get("/accounts/customer/" + uuid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(uuid.toString()))
                .andExpect(jsonPath("$[0].balance").value(1000.0));
    }

    @Test
    void testDeposit() throws Exception {
        TransactionRequest request = new TransactionRequest(500.0, "ACC002");

        Account updated = new Account();
        updated.setId(1L);
        updated.setUserId(uuid);
        updated.setAccountNumber("ACC002");
        updated.setBalance(1500.0);
        updated.setAccountType("SAVINGS");
        updated.setIsActive(true);
        updated.setCreatedAt(new Date());

        Mockito.when(accountService.deposit(any(TransactionRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.0));
    }

    @Test
    void testWithdraw() throws Exception {
        TransactionRequest request = new TransactionRequest(200.0, "ACC003");

        Account updated = new Account();
        updated.setId(1L);
        updated.setUserId(uuid);
        updated.setAccountNumber("ACC003");
        updated.setBalance(800.0);
        updated.setAccountType("SAVINGS");
        updated.setIsActive(true);
        updated.setCreatedAt(new Date());

        Mockito.when(accountService.withdraw(any(TransactionRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.0));
    }

    @Test
    void testTransfer() throws Exception {
        TransferRequest request = new TransferRequest("ABC", "DEF", 100.0);

        // Tạo object giả để mock
        Account updated = new Account();
        updated.setBalance(800.0);

        Mockito.doNothing().when(accountService).transfer(any(TransferRequest.class));

        mockMvc.perform(post("/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // .andExpect(jsonPath("$.balance").value(800.0)); // Câu này sẽ fail nếu controller không trả JSON
    }


}
