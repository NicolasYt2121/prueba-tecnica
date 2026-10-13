package com.nexo.banking.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.nexo.banking.entity.Types.*;
public final class Requests {
    private Requests() {}
    public record CustomerInput(
        @NotNull IdentificationType identificationType,
        @NotBlank @Pattern(regexp="[A-Za-z0-9-]{4,25}") String identificationNumber,
        @NotBlank @Size(min=2,max=80) String firstName,
        @NotBlank @Size(min=2,max=80) String lastName,
        @NotBlank @Email @Pattern(regexp="^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") @Size(max=160) String email,
        @NotNull @Past LocalDate birthDate) {}
    public record AccountInput(@NotNull @Positive Long customerId,@NotNull AccountType type, boolean gmfExempt) {}
    public record AccountUpdate(@NotNull AccountStatus status,boolean gmfExempt) {}
    public record TransactionInput(@NotNull UUID requestId,@NotNull TransactionType type,
        @Positive Long sourceAccountId,@Positive Long destinationAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal amount,
        @NotNull @Size(max=250) String description) {}
    public record TransactionUpdate(@NotNull @Size(max=250) String description) {}
}
