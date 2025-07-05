package com.devteria.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreatedEvent {
    private UUID id;
    private String username;
    private String email;
    private Date createdAt;
}
