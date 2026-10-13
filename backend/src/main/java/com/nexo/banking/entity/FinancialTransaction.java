package com.nexo.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.nexo.banking.domain.model.Types.TransactionType;

@Entity @Table(name="financial_transactions")
public class FinancialTransaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false,unique=true) public UUID requestId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public TransactionType type;
    public Long sourceAccountId;
    public Long destinationAccountId;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal amount;
    @Column(nullable=false,length=250) public String description;
    @Column(nullable=false) public Instant createdAt;
    @Column(nullable=false) public Instant updatedAt;
    public boolean reversed;
    public Instant reversedAt;
    @Version public long version;
}
