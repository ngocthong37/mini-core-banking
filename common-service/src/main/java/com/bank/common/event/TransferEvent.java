package com.bank.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferEvent {
    private String type;
    private String fromAccount;
    private String toAccount;
    private Double amount;
    private Date createdAt;
}
