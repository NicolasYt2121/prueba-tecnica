package com.nexo.banking.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.nexo.banking.domain.model.Types.TransactionType;

public class FinancialTransaction {
    public Long id;
    public UUID requestId;
    public TransactionType type;
    public Long sourceAccountId;
    public Long destinationAccountId;
    public BigDecimal amount;
    public String description;
    public Instant createdAt;
    public Instant updatedAt;
    public boolean reversed;
    public Instant reversedAt;
    public long version;
}
