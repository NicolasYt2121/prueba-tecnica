package com.nexo.banking.config;
import com.nexo.banking.application.port.in.*;
import com.nexo.banking.application.port.out.*;
import com.nexo.banking.service.*;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root: the core is constructed with output ports and wrapped in transactions. */
@Configuration
public class UseCaseConfiguration {
    @Bean CustomerUseCases customers(CustomerRepository customers,AccountRepository accounts,Clock clock) {
        return new TransactionalCustomerUseCases(new CustomerService(customers,accounts,clock));
    }
    @Bean AccountUseCases accounts(AccountRepository accounts,CustomerRepository customers,MovementRepository movements,AccountNumberGenerator numbers,Clock clock) {
        return new TransactionalAccountUseCases(new AccountService(accounts,customers,movements,numbers,clock));
    }
    @Bean TransactionUseCases transactions(TransactionRepository transactions,AccountRepository accounts,MovementRepository movements,Clock clock) {
        return new TransactionalTransactionUseCases(new TransactionService(transactions,accounts,movements,clock));
    }
}
