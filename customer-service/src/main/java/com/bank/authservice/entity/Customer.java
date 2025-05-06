
package com.bank.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String nationalId;

    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String address;
    private Boolean isActive = true;

    private Date createdAt;
    private Date updatedAt;

}
