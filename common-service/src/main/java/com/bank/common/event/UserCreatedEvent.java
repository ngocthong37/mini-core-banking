package com.bank.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent {
    private String id;
    private String username;
    private String email;
    private Date createdAt;
}
