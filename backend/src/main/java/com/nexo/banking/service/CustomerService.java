package com.nexo.banking.service;
import com.nexo.banking.application.port.in.CustomerUseCases;
import com.nexo.banking.domain.model.Customer;
import com.nexo.banking.application.Commands.CustomerInput;
import com.nexo.banking.application.port.out.*;
import java.time.*;
import java.util.List;
public class CustomerService implements CustomerUseCases {
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final Clock clock;
    public CustomerService(CustomerRepository customers,AccountRepository accounts,Clock clock) {
        this.customers=customers; this.accounts=accounts; this.clock=clock;
    }
    public List<Customer> list() { return customers.findAll(); }
    public Customer get(Long id) { return customers.findById(id).orElseThrow(()->BusinessException.notFound("Cliente")); }
    public Customer create(CustomerInput input) {
        Customer c=new Customer(); c.createdAt=clock.instant(); fill(c,input); return customers.save(c);
    }
    public Customer update(Long id,CustomerInput input) {
        Customer c=customers.lockById(id).orElseThrow(()->BusinessException.notFound("Cliente")); fill(c,input); return customers.save(c);
    }
    public void delete(Long id) {
        Customer c=customers.lockById(id).orElseThrow(()->BusinessException.notFound("Cliente"));
        if(accounts.existsByCustomerId(id)) throw BusinessException.conflict("El cliente tiene productos vinculados, incluso si estÃ¡n cancelados");
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
