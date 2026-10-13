package com.nexo.banking.service;
import com.nexo.banking.application.Commands.CustomerInput;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.domain.model.Types.*;
import com.nexo.banking.application.port.out.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock CustomerRepository customers; @Mock AccountRepository accounts;
    final Clock clock=Clock.fixed(Instant.parse("2026-10-13T15:00:00Z"),ZoneId.of("America/Bogota"));
    CustomerService service;
    CustomerInput input(LocalDate birth) { return new CustomerInput(IdentificationType.CC,"123456"," Ana "," PÃ©rez ","ana@example.com",birth); }
    @BeforeEach void setup() { service=new CustomerService(customers,accounts,clock); }
    @Test void createsAdultOnEighteenthBirthdayWithAutomaticDates() {
        when(customers.save(any())).thenAnswer(i->i.getArgument(0));
        Customer c=service.create(input(LocalDate.of(2008,10,13)));
        assertThat(c.firstName).isEqualTo("Ana");assertThat(c.createdAt).isEqualTo(clock.instant());assertThat(c.updatedAt).isEqualTo(clock.instant());
    }
    @Test void rejectsOneDayUnderage() { assertThatThrownBy(()->service.create(input(LocalDate.of(2008,10,14)))).isInstanceOf(BusinessException.class);verify(customers,never()).save(any()); }
    @Test void rejectsWhitespaceNames() { var in=new CustomerInput(IdentificationType.CC,"1234"," a ","Perez","a@b.co",LocalDate.of(1990,1,1));assertThatThrownBy(()->service.create(in)).hasMessageContaining("2 caracteres"); }
    @Test void updatesLastModificationButPreservesCreation() {
        Customer c=new Customer();c.id=1L;c.createdAt=Instant.EPOCH;when(customers.lockById(1L)).thenReturn(Optional.of(c));when(customers.save(c)).thenReturn(c);
        service.update(1L,input(LocalDate.of(1990,1,1)));assertThat(c.updatedAt).isEqualTo(clock.instant());assertThat(c.createdAt).isEqualTo(Instant.EPOCH);
    }
    @Test void doesNotDeleteWithAnyProduct() { Customer c=new Customer();when(customers.lockById(1L)).thenReturn(Optional.of(c));when(accounts.existsByCustomerId(1L)).thenReturn(true);assertThatThrownBy(()->service.delete(1L)).hasMessageContaining("productos");verify(customers,never()).delete(any()); }
    @Test void deletesWithoutProducts() { Customer c=new Customer();when(customers.lockById(1L)).thenReturn(Optional.of(c));service.delete(1L);verify(customers).delete(c); }
    @Test void retrievesAndLists() { Customer c=new Customer();when(customers.findById(1L)).thenReturn(Optional.of(c));assertThat(service.get(1L)).isSameAs(c);service.list();verify(customers).findAll(); }
    @Test void missingCustomerIs404() { assertThatThrownBy(()->service.get(99L)).isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.code).isEqualTo(BusinessException.Code.NOT_FOUND));assertThatThrownBy(()->service.update(99L,input(LocalDate.of(1990,1,1)))).isInstanceOf(BusinessException.class);assertThatThrownBy(()->service.delete(99L)).isInstanceOf(BusinessException.class); }
}
