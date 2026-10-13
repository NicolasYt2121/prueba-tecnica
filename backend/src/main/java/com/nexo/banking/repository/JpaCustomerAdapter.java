package com.nexo.banking.repository;
import com.nexo.banking.domain.model.Customer;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

@Component
public class JpaCustomerAdapter implements com.nexo.banking.application.port.out.CustomerRepository {
    private final CustomerRepository repository;
    public JpaCustomerAdapter(CustomerRepository repository) { this.repository=repository; }
    @Override public List<Customer> findAll() { return repository.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(JpaCustomerAdapter::toDomain).toList(); }
    @Override public Optional<Customer> findById(Long id) { return repository.findById(id).map(JpaCustomerAdapter::toDomain); }
    @Override public Optional<Customer> lockById(Long id) { return repository.lockById(id).map(JpaCustomerAdapter::toDomain); }
    @Override public Customer save(Customer value) {
        var saved=repository.save(toEntity(value));
        value.id=saved.id;
        value.version=saved.version;
        return toDomain(saved);
    }
    @Override public void delete(Customer value) { repository.delete(toEntity(value)); }
    private static Customer toDomain(com.nexo.banking.entity.Customer source) {
        var target=new Customer();
        target.id=source.id;
        target.identificationType=source.identificationType;
        target.identificationNumber=source.identificationNumber;
        target.firstName=source.firstName;
        target.lastName=source.lastName;
        target.email=source.email;
        target.birthDate=source.birthDate;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.version=source.version;
        return target;
    }
    private static com.nexo.banking.entity.Customer toEntity(Customer source) {
        var target=new com.nexo.banking.entity.Customer();
        target.id=source.id;
        target.identificationType=source.identificationType;
        target.identificationNumber=source.identificationNumber;
        target.firstName=source.firstName;
        target.lastName=source.lastName;
        target.email=source.email;
        target.birthDate=source.birthDate;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.version=source.version;
        return target;
    }
}
