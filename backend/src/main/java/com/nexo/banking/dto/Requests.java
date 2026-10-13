package com.nexo.banking.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.nexo.banking.domain.model.Types.*;
public final class Requests {
    private Requests() {}
    public record CustomerInput(
        @NotNull IdentificationType identificationType,
        @NotBlank @Pattern(regexp="[A-Za-z0-9-]{4,25}") String identificationNumber,
        @NotBlank @Size(min=2,max=80) String firstName,
        @NotBlank @Size(min=2,max=80) String lastName,
        @NotBlank @Email @Pattern(regexp="^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") @Size(max=160) String email,
        @NotNull @Past LocalDate birthDate) { public com.nexo.banking.application.Commands.CustomerInput toCommand() { return new com.nexo.banking.application.Commands.CustomerInput(identificationType,identificationNumber,firstName,lastName,email,birthDate); } }
    public record AccountInput(@NotNull @Positive Long customerId,@NotNull AccountType type, boolean gmfExempt) { public com.nexo.banking.application.Commands.AccountInput toCommand() { return new com.nexo.banking.application.Commands.AccountInput(customerId,type,gmfExempt); } }
    public record AccountUpdate(@NotNull AccountStatus status,boolean gmfExempt) { public com.nexo.banking.application.Commands.AccountUpdate toCommand() { return new com.nexo.banking.application.Commands.AccountUpdate(status,gmfExempt); } }
    public record TransactionInput(@NotNull UUID requestId,@NotNull TransactionType type,
        @Positive Long sourceAccountId,@Positive Long destinationAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,
        @NotNull @Size(max=250) String description) { public com.nexo.banking.application.Commands.TransactionInput toCommand() { return new com.nexo.banking.application.Commands.TransactionInput(requestId,type,sourceAccountId,destinationAccountId,amount,description); } }
    public record TransactionUpdate(@NotNull @Size(max=250) String description) { public com.nexo.banking.application.Commands.TransactionUpdate toCommand() { return new com.nexo.banking.application.Commands.TransactionUpdate(description); } }
}

