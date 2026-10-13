package com.nexo.banking.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import com.nexo.banking.domain.model.Types.*;

public class Account {
    public Long id;
    public Long customerId;
    public AccountType type;
    public String number;
    public AccountStatus status;
    public BigDecimal balance = new BigDecimal("0.00");
    public BigDecimal availableBalance = new BigDecimal("0.00");
    public boolean gmfExempt;
    public Instant createdAt;
    public Instant updatedAt;
    public long version;
}
