package com.nexo.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="movements")
public class Movement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long transactionId;
    @Column(nullable=false) public Long accountId;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal delta;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal balanceAfter;
    public boolean reversal;
    @Column(nullable=false) public Instant createdAt;
}
