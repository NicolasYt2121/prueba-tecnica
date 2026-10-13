package com.nexo.banking.service;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.entity.*;
import com.nexo.banking.entity.Types.*;
import com.nexo.banking.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock AccountRepository accounts; @Mock CustomerRepository customers; @Mock MovementRepository movements; @Mock AccountNumberGenerator numbers;
    Clock clock=Clock.fixed(Instant.parse("2026-10-13T15:00:00Z"),ZoneOffset.UTC); AccountService service;
    @BeforeEach void setup() { service=new AccountService(accounts,customers,movements,numbers,clock); }
    Account account() { Account a=new Account();a.id=1L;a.status=AccountStatus.ACTIVA;a.type=AccountType.AHORROS;return a; }
    @ParameterizedTest @EnumSource(AccountType.class) void createsBothAccountTypesAtZero(AccountType type) {
        when(customers.lockById(1L)).thenReturn(Optional.of(new Customer()));when(numbers.next(type)).thenReturn(type==AccountType.AHORROS?"5300000001":"3300000001");when(accounts.save(any())).thenAnswer(i->i.getArgument(0));
        Account a=service.create(new AccountInput(1L,type,true));assertThat(a.number).matches(type==AccountType.AHORROS?"53\\d{8}":"33\\d{8}");assertThat(a.status).isEqualTo(AccountStatus.ACTIVA);assertThat(a.balance).isEqualByComparingTo("0");assertThat(a.availableBalance).isEqualByComparingTo("0");assertThat(a.createdAt).isEqualTo(clock.instant());
    }
    @Test void noOrphanAccounts() { assertThatThrownBy(()->service.create(new AccountInput(99L,AccountType.AHORROS,false))).hasMessageContaining("Cliente");verify(accounts,never()).save(any()); }
    @Test void canDeactivateAndReactivate() { Account a=account();when(accounts.lockById(1L)).thenReturn(Optional.of(a));when(accounts.save(a)).thenReturn(a);service.update(1L,new AccountUpdate(AccountStatus.INACTIVA,true));assertThat(a.status).isEqualTo(AccountStatus.INACTIVA);service.update(1L,new AccountUpdate(AccountStatus.ACTIVA,false));assertThat(a.status).isEqualTo(AccountStatus.ACTIVA);assertThat(a.updatedAt).isEqualTo(clock.instant()); }
    @Test void rejectsCancelWithPositiveOrNegativeBalance() { Account a=account();when(accounts.lockById(1L)).thenReturn(Optional.of(a));for(String balance:List.of("1.00","-1.00")) {a.balance=new BigDecimal(balance);assertThatThrownBy(()->service.delete(1L)).hasMessageContaining("saldo cero");} }
    @Test void cancelsAtZeroAndCannotReopen() { Account a=account();when(accounts.lockById(1L)).thenReturn(Optional.of(a));service.delete(1L);assertThat(a.status).isEqualTo(AccountStatus.CANCELADA);assertThatThrownBy(()->service.update(1L,new AccountUpdate(AccountStatus.ACTIVA,false))).hasMessageContaining("reabrirse"); }
    @Test void listsAndFiltersByCustomer() { service.list(null);service.list(1L);verify(accounts).findAll(any(org.springframework.data.domain.Sort.class));verify(accounts).findByCustomerIdOrderByIdDesc(1L); }
    @Test void returnsStatement() { Account a=account();Movement m=new Movement();when(accounts.findById(1L)).thenReturn(Optional.of(a));when(movements.findByAccountIdOrderByIdAsc(1L)).thenReturn(List.of(m));var s=service.statement(1L);assertThat(s.account()).isSameAs(a);assertThat(s.movements()).containsExactly(m); }
    @Test void missingAccountIs404() { assertThatThrownBy(()->service.get(99L)).isInstanceOf(BusinessException.class);assertThatThrownBy(()->service.update(99L,new AccountUpdate(AccountStatus.ACTIVA,false))).isInstanceOf(BusinessException.class);assertThatThrownBy(()->service.delete(99L)).isInstanceOf(BusinessException.class); }
}
