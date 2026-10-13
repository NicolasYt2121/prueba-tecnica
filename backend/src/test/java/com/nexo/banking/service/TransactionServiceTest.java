package com.nexo.banking.service;
import com.nexo.banking.application.Commands.*;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.domain.model.Types.*;
import com.nexo.banking.application.port.out.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock TransactionRepository transactions; @Mock AccountRepository accounts; @Mock MovementRepository movements;
    Clock clock=Clock.fixed(Instant.parse("2026-10-13T15:00:00Z"),ZoneOffset.UTC);TransactionService service;
    @BeforeEach void setup() {service=new TransactionService(transactions,accounts,movements,clock);}
    Account account(long id,String balance,AccountType type) {Account a=new Account();a.id=id;a.number="530000000"+id;a.balance=new BigDecimal(balance);a.availableBalance=a.balance;a.type=type;a.status=AccountStatus.ACTIVA;when(accounts.lockById(id)).thenReturn(Optional.of(a));return a;}
    TransactionInput input(TransactionType type,Long source,Long target,String amount) {return new TransactionInput(UUID.randomUUID(),type,source,target,new BigDecimal(amount),"Prueba");}
    void save() {when(transactions.saveAndFlush(any())).thenAnswer(i->{FinancialTransaction t=i.getArgument(0);t.id=7L;return t;});}
    @Test void depositUpdatesBothBalancesAndCredit() {Account a=account(1,"20",AccountType.AHORROS);save();service.create(input(TransactionType.CONSIGNACION,null,1L,"10.25"));assertThat(a.balance).isEqualByComparingTo("30.25");assertThat(a.availableBalance).isEqualTo(a.balance);ArgumentCaptor<Movement> c=ArgumentCaptor.forClass(Movement.class);verify(movements).save(c.capture());assertThat(c.getValue().delta).isEqualByComparingTo("10.25");}
    @Test void withdrawalDebits() {Account a=account(1,"20",AccountType.AHORROS);save();service.create(input(TransactionType.RETIRO,1L,null,"20"));assertThat(a.balance).isEqualByComparingTo("0");}
    @Test void transferLocksInAscendingOrderAndCreatesTwoEntries() {Account a=account(2,"100",AccountType.AHORROS),b=account(1,"0",AccountType.AHORROS);save();service.create(input(TransactionType.TRANSFERENCIA,2L,1L,"40"));assertThat(a.balance).isEqualByComparingTo("60");assertThat(b.balance).isEqualByComparingTo("40");InOrder order=inOrder(accounts);order.verify(accounts).lockById(1L);order.verify(accounts).lockById(2L);verify(movements,times(2)).save(any());}
    @Test void insufficientSavingsNeverMutatesEitherBalance() {Account a=account(1,"5",AccountType.AHORROS),b=account(2,"10",AccountType.AHORROS);save();assertThatThrownBy(()->service.create(input(TransactionType.TRANSFERENCIA,1L,2L,"6"))).hasMessageContaining("Fondos");assertThat(a.balance).isEqualByComparingTo("5");assertThat(b.balance).isEqualByComparingTo("10");verify(movements,never()).save(any());}
    @Test void currentAccountMayOverdraw() {Account a=account(1,"0",AccountType.CORRIENTE);save();service.create(input(TransactionType.RETIRO,1L,null,"10"));assertThat(a.balance).isEqualByComparingTo("-10");}
    @ParameterizedTest @ValueSource(strings={"0","-1","0.001","100000000000000000.00"}) void rejectsBadAmounts(String amount) {assertThatThrownBy(()->service.create(input(TransactionType.RETIRO,1L,null,amount))).isInstanceOf(BusinessException.class);verifyNoInteractions(accounts,movements);}
    @Test void rejectsWrongAccountCombinations() {for(TransactionInput in:List.of(input(TransactionType.TRANSFERENCIA,1L,1L,"1"),input(TransactionType.TRANSFERENCIA,1L,null,"1"),input(TransactionType.CONSIGNACION,1L,2L,"1"),input(TransactionType.RETIRO,null,2L,"1"))) assertThatThrownBy(()->service.create(in)).hasMessageContaining("incompatibles");}
    @Test void rejectsInactiveAndMissingAccounts() {Account a=account(1,"20",AccountType.AHORROS);a.status=AccountStatus.INACTIVA;assertThatThrownBy(()->service.create(input(TransactionType.RETIRO,1L,null,"1"))).hasMessageContaining("activa");assertThatThrownBy(()->service.create(input(TransactionType.RETIRO,99L,null,"1"))).hasMessageContaining("no encontrado");}
    @Test void preventsNumericOverflow() {account(1,"99999999999999999.99",AccountType.AHORROS);save();assertThatThrownBy(()->service.create(input(TransactionType.CONSIGNACION,null,1L,"1"))).hasMessageContaining("lÃ­mite");}
    @Test void idempotentRetryDoesNotDebitTwice() {var in=input(TransactionType.RETIRO,1L,null,"10");FinancialTransaction t=new FinancialTransaction();t.requestId=in.requestId();t.type=in.type();t.sourceAccountId=1L;t.amount=in.amount();when(transactions.findByRequestId(in.requestId())).thenReturn(Optional.of(t));assertThat(service.create(in)).isSameAs(t);verifyNoInteractions(accounts,movements);assertThatThrownBy(()->service.create(new TransactionInput(in.requestId(),in.type(),1L,null,new BigDecimal("11"),""))).hasMessageContaining("idempotencia");}
    @Test void reversesTransferAndSecondDeleteDoesNothing() {Account a=account(1,"60",AccountType.AHORROS),b=account(2,"40",AccountType.AHORROS);FinancialTransaction t=new FinancialTransaction();t.id=7L;t.sourceAccountId=1L;t.destinationAccountId=2L;t.amount=new BigDecimal("40");when(transactions.lockById(7L)).thenReturn(Optional.of(t));service.delete(7L);service.delete(7L);assertThat(a.balance).isEqualByComparingTo("100");assertThat(b.balance).isEqualByComparingTo("0");assertThat(t.reversed).isTrue();assertThat(t.reversedAt).isEqualTo(clock.instant());verify(movements,times(2)).save(argThat(m->m.reversal));}
    @Test void updatesOnlyDescriptionAndRejectsReversed() {FinancialTransaction t=new FinancialTransaction();when(transactions.lockById(7L)).thenReturn(Optional.of(t));service.update(7L,new TransactionUpdate(" Nota "));assertThat(t.description).isEqualTo("Nota");t.reversed=true;assertThatThrownBy(()->service.update(7L,new TransactionUpdate("Otro"))).hasMessageContaining("anulada");}
    @Test void readsAndListsAndHandlesMissing() {FinancialTransaction t=new FinancialTransaction();when(transactions.findById(7L)).thenReturn(Optional.of(t));assertThat(service.get(7L)).isSameAs(t);service.list();assertThatThrownBy(()->service.get(99L)).isInstanceOf(BusinessException.class);assertThatThrownBy(()->service.update(99L,new TransactionUpdate(""))).isInstanceOf(BusinessException.class);assertThatThrownBy(()->service.delete(99L)).isInstanceOf(BusinessException.class);}
}
