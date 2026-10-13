package com.nexo.banking;

import com.nexo.banking.application.Commands.*;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.domain.model.Types.*;
import com.nexo.banking.service.*;
import com.nexo.banking.application.port.in.*;
import com.nexo.banking.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@EnabledIfEnvironmentVariable(named="TEST_DB_URL",matches=".*")
class BankingIT {
    @DynamicPropertySource static void db(DynamicPropertyRegistry registry) {
        String url=System.getenv("TEST_DB_URL");
        if(!url.matches("jdbc:postgresql://[^/]+/[A-Za-z0-9_]*test(?:\\?.*)?")) throw new IllegalArgumentException("TEST_DB_URL debe apuntar a una base dedicada cuyo nombre termine en test");
        registry.add("spring.datasource.url",()->url);
    }
    @Autowired CustomerUseCases customers; @Autowired AccountUseCases accounts; @Autowired TransactionUseCases transactions;
    @Autowired TransactionRepository transactionRepository; @Autowired MovementRepository movementRepository; @Autowired JdbcTemplate jdbc;
    Long owner;
    @BeforeEach void prepare() {
        jdbc.execute("TRUNCATE movements,financial_transactions,accounts,customers RESTART IDENTITY CASCADE");
        owner=customers.create(new CustomerInput(IdentificationType.CC,"900001","Cliente","Prueba","test@example.com",LocalDate.of(1990,1,1))).id;
    }
    Account account(AccountType type) { return accounts.create(new AccountInput(owner,type,false)); }
    FinancialTransaction deposit(Long account,String amount) {return transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.CONSIGNACION,null,account,new BigDecimal(amount),"Prueba de integraciÃ³n"));}
    @Test void postgresGeneratesUniqueNumbersAndEnforcesAdultAndForeignKey() {
        Account a=account(AccountType.AHORROS),b=account(AccountType.CORRIENTE);
        assertThat(a.number).matches("53\\d{8}");assertThat(b.number).matches("33\\d{8}");assertThat(a.number).isNotEqualTo(b.number);
        assertThatThrownBy(()->jdbc.update("UPDATE customers SET birth_date=CURRENT_DATE WHERE id=?",owner)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->jdbc.update("DELETE FROM customers WHERE id=?",owner)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->jdbc.update("UPDATE accounts SET balance=-1,available_balance=-1 WHERE id=?",a.id)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->jdbc.update("UPDATE accounts SET customer_id=99999 WHERE id=?",a.id)).isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(()->jdbc.update("UPDATE accounts SET number=? WHERE id=?",a.number,b.id)).isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test void transferAndReversalConserveMoneyAndProduceTwoMovements() {
        Account a=account(AccountType.AHORROS),b=account(AccountType.AHORROS);deposit(a.id,"100.00");
        var t=transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.TRANSFERENCIA,a.id,b.id,new BigDecimal("35.25"),"Transferencia"));
        assertThat(accounts.get(a.id).balance).isEqualByComparingTo("64.75");assertThat(accounts.get(b.id).balance).isEqualByComparingTo("35.25");
        assertThat(accounts.statement(b.id).movements()).hasSize(1);transactions.delete(t.id);transactions.delete(t.id);
        assertThat(accounts.get(a.id).balance).isEqualByComparingTo("100");assertThat(accounts.get(b.id).balance).isEqualByComparingTo("0");assertThat(accounts.statement(b.id).movements()).hasSize(2);assertThat(transactions.get(t.id).reversed).isTrue();
    }
    @Test void insufficientFundsRollBackTransactionAndAllMovements() {
        Account a=account(AccountType.AHORROS),b=account(AccountType.AHORROS);deposit(a.id,"10");long before=transactionRepository.count();long entries=movementRepository.count();
        assertThatThrownBy(()->transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.TRANSFERENCIA,a.id,b.id,new BigDecimal("11"),"Fallida"))).isInstanceOf(BusinessException.class);
        assertThat(transactionRepository.count()).isEqualTo(before);assertThat(movementRepository.count()).isEqualTo(entries);assertThat(accounts.get(a.id).balance).isEqualByComparingTo("10");assertThat(accounts.get(b.id).balance).isEqualByComparingTo("0");
    }
    @Test void simultaneousWithdrawalsCannotOverdrawSavings() throws Exception {
        Account a=account(AccountType.AHORROS);deposit(a.id,"100");CountDownLatch start=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> results=new ArrayList<>();for(int i=0;i<2;i++)results.add(executor.submit(()->{start.await();try {transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.RETIRO,a.id,null,new BigDecimal("80"),"Concurrente"));return true;}catch(BusinessException ex){return false;}}));
            start.countDown();int successes=0;for(var r:results)if(r.get(15,TimeUnit.SECONDS))successes++;assertThat(successes).isEqualTo(1);
        }
        assertThat(accounts.get(a.id).balance).isEqualByComparingTo("20");assertThat(accounts.get(a.id).availableBalance).isEqualByComparingTo("20");
    }
    @Test void concurrentDuplicateKeyCannotPostTwice() throws Exception {
        Account a=account(AccountType.AHORROS);var in=new TransactionInput(UUID.randomUUID(),TransactionType.CONSIGNACION,null,a.id,new BigDecimal("25"),"Idempotente");CountDownLatch start=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(2)) {
            List<Future<?>> results=new ArrayList<>();for(int i=0;i<2;i++)results.add(executor.submit(()->{try {start.await();transactions.create(in);}catch(DataIntegrityViolationException expected){}catch(InterruptedException ex){Thread.currentThread().interrupt();throw new RuntimeException(ex);}}));
            start.countDown();for(var r:results)r.get(15,TimeUnit.SECONDS);
        }
        transactions.create(in);assertThat(accounts.get(a.id).balance).isEqualByComparingTo("25");assertThat(transactionRepository.count()).isEqualTo(1);assertThat(movementRepository.count()).isEqualTo(1);
    }
    @Test void failedReversalDoesNotPartiallyCreditSender() {
        Account a=account(AccountType.AHORROS),b=account(AccountType.AHORROS);deposit(a.id,"50");var t=transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.TRANSFERENCIA,a.id,b.id,new BigDecimal("50"),""));transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.RETIRO,b.id,null,new BigDecimal("50"),""));
        assertThatThrownBy(()->transactions.delete(t.id)).isInstanceOf(BusinessException.class);assertThat(accounts.get(a.id).balance).isEqualByComparingTo("0");assertThat(accounts.get(b.id).balance).isEqualByComparingTo("0");assertThat(transactions.get(t.id).reversed).isFalse();
    }
    @Test void cancellationAndInactiveAccountRulesArePersisted() {
        Account a=account(AccountType.AHORROS);accounts.update(a.id,new AccountUpdate(AccountStatus.INACTIVA,true));assertThatThrownBy(()->deposit(a.id,"1")).isInstanceOf(BusinessException.class);accounts.update(a.id,new AccountUpdate(AccountStatus.ACTIVA,true));deposit(a.id,"1");assertThatThrownBy(()->accounts.delete(a.id)).isInstanceOf(BusinessException.class);transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.RETIRO,a.id,null,new BigDecimal("1"),""));accounts.delete(a.id);assertThat(accounts.get(a.id).status).isEqualTo(AccountStatus.CANCELADA);assertThatThrownBy(()->customers.delete(owner)).isInstanceOf(BusinessException.class);
    }
    @Test void currentAccountsAllowNegativeBalance() {Account a=account(AccountType.CORRIENTE);transactions.create(new TransactionInput(UUID.randomUUID(),TransactionType.RETIRO,a.id,null,new BigDecimal("12.50"),""));assertThat(accounts.get(a.id).balance).isEqualByComparingTo("-12.50");}
}
