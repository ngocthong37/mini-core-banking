package com.bank.accountservice.controller;

import com.bank.accountservice.dto.*;
import com.bank.accountservice.entity.Account;
import com.bank.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public Account create(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public Account get(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Account> getByCustomer(@PathVariable UUID customerId) {
        return accountService.getAccountsByCustomer(customerId);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/deposit")
    public Account deposit(@RequestBody TransactionRequest request) {
        return accountService.deposit(request);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/withdraw")
    public Account withdraw(@RequestBody TransactionRequest request) {
        return accountService.withdraw(request);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public void transfer(@RequestBody TransferRequest request) {
        accountService.transfer(request);
    }

    @GetMapping("/get-all")
    public ApiResponse<List<AccountResponse>> getAllAccount() {
        List<AccountResponse> data =  accountService.getAllAccount();
        return ApiResponse.<List<AccountResponse>>builder()
                .code(1000)
                .message("Success")
                .result(data)
                .build();
    }
}
