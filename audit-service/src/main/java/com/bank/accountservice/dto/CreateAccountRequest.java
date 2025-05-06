package com.bank.accountservice.dto;

import lombok.Data;

@Data
public class CreateAccountRequest {
    private Long customerId;
    private String accountType;
}
