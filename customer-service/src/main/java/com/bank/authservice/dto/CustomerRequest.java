
package com.bank.authservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    private String fullName;
    private String nationalId;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String address;
}
