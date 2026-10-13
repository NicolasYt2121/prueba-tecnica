package com.nexo.banking.domain.model;


import java.math.BigDecimal;
import java.time.Instant;

public class Movement {
    public Long id;
    public Long transactionId;
    public Long accountId;
    public BigDecimal delta;
    public BigDecimal balanceAfter;
    public boolean reversal;
    public Instant createdAt;
}
