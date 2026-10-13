package com.nexo.banking.service;
import com.nexo.banking.entity.Customer;
import com.nexo.banking.dto.Requests.CustomerInput;
import com.nexo.banking.repository.*;
import java.time.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class CustomerService {
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final Clock clock;
    public CustomerService(CustomerRepository customers,AccountRepository accounts,Clock clock) {
        this.customers=customers; this.accounts=accounts; this.clock=clock;
    }
    public List<Customer> list() { return customers.findAll(Sort.by(Sort.Direction.DESC,"id")); }
    public Customer get(Long id) { return customers.findById(id).orElseThrow(()->BusinessException.notFound("Cliente")); }
    @Transactional public Customer create(CustomerInput input) {
        Customer c=new Customer(); c.createdAt=clock.instant(); fill(c,input); return customers.save(c);
    }
    @Transactional public Customer update(Long id,CustomerInput input) {
        Customer c=customers.lockById(id).orElseThrow(()->BusinessException.notFound("Cliente")); fill(c,input); return customers.save(c);
    }
    @Transactional public void delete(Long id) {
        Customer c=customers.lockById(id).orElseThrow(()->BusinessException.notFound("Cliente"));
        if(accounts.existsByCustomerId(id)) throw BusinessException.conflict("El cliente tiene productos vinculados, incluso si están cancelados");
        customers.delete(c);
    }
    private void fill(Customer c,CustomerInput in) {
        if(in.birthDate().isAfter(LocalDate.now(clock).minusYears(18))) throw BusinessException.invalid("El cliente debe ser mayor de edad");
        if(in.firstName().strip().length()<2 || in.lastName().strip().length()<2) throw BusinessException.invalid("Nombres y apellidos requieren al menos 2 caracteres");
        c.identificationType=in.identificationType(); c.identificationNumber=in.identificationNumber().strip();
        c.firstName=in.firstName().strip(); c.lastName=in.lastName().strip(); c.email=in.email().strip();
        c.birthDate=in.birthDate(); c.updatedAt=clock.instant();
    }
}
