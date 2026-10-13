package com.nexo.banking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import com.nexo.banking.entity.Types.*;

@Entity @Table(name="accounts")
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(nullable=false) public Long customerId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public AccountType type;
    @Column(nullable=false,unique=true,length=10) public String number;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public AccountStatus status;
    @Column(nullable=false,precision=19,scale=2) public BigDecimal balance = new BigDecimal("0.00");
    @Column(nullable=false,precision=19,scale=2) public BigDecimal availableBalance = new BigDecimal("0.00");
    public boolean gmfExempt;
    @Column(nullable=false) public Instant createdAt;
    @Column(nullable=false) public Instant updatedAt;
    @Version public long version;
}
