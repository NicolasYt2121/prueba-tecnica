package com.nexo.banking.application;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.nexo.banking.domain.model.Types.*;

/** Framework-free input values for application use cases. */
public final class Commands {
    private Commands() {}
    public record CustomerInput(IdentificationType identificationType,String identificationNumber,String firstName,String lastName,String email,LocalDate birthDate) {}
    public record AccountInput(Long customerId,AccountType type,boolean gmfExempt) {}
    public record AccountUpdate(AccountStatus status,boolean gmfExempt) {}
    public record TransactionInput(UUID requestId,TransactionType type,Long sourceAccountId,Long destinationAccountId,BigDecimal amount,String description) {}
    public record TransactionUpdate(String description) {}
}
