package com.nexo.banking.entity;

import jakarta.persistence.*;
import java.time.*;
import com.nexo.banking.domain.model.Types.IdentificationType;

@Entity @Table(name="customers")
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public IdentificationType identificationType;
    @Column(nullable=false) public String identificationNumber;
    @Column(nullable=false) public String firstName;
    @Column(nullable=false) public String lastName;
    @Column(nullable=false) public String email;
    @Column(nullable=false) public LocalDate birthDate;
    @Column(nullable=false) public Instant createdAt;
    @Column(nullable=false) public Instant updatedAt;
    @Version public long version;
}
