package com.bank.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String to;
    private String[] cc;
    private String subject;
    private Map<String, Object> model;
}
