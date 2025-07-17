package com.devteria.event.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private String to;
    private String[] cc;
    private String subject;
    private Map<String, Object> model;
}
