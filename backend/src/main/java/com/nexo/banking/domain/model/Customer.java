package com.nexo.banking.domain.model;


import java.time.*;
import com.nexo.banking.domain.model.Types.IdentificationType;

public class Customer {
    public Long id;
    public IdentificationType identificationType;
    public String identificationNumber;
    public String firstName;
    public String lastName;
    public String email;
    public LocalDate birthDate;
    public Instant createdAt;
    public Instant updatedAt;
    public long version;
}
