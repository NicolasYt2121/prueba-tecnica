package com.nexo.banking.repository;
import com.nexo.banking.domain.model.Account;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

@Component
public class JpaAccountAdapter implements com.nexo.banking.application.port.out.AccountRepository {
    private final AccountRepository repository;
    public JpaAccountAdapter(AccountRepository repository) { this.repository=repository; }
    @Override public List<Account> findAll() { return repository.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(JpaAccountAdapter::toDomain).toList(); }
    @Override public Optional<Account> findById(Long id) { return repository.findById(id).map(JpaAccountAdapter::toDomain); }
    @Override public Optional<Account> lockById(Long id) { return repository.lockById(id).map(JpaAccountAdapter::toDomain); }
    @Override public Account save(Account value) {
        var saved=repository.save(toEntity(value));
        value.id=saved.id;
        value.version=saved.version;
        return toDomain(saved);
    }
    @Override public boolean existsByCustomerId(Long id) { return repository.existsByCustomerId(id); }
    @Override public List<Account> findByCustomerIdOrderByIdDesc(Long id) { return repository.findByCustomerIdOrderByIdDesc(id).stream().map(JpaAccountAdapter::toDomain).toList(); }
    private static Account toDomain(com.nexo.banking.entity.Account source) {
        var target=new Account();
        target.id=source.id;
        target.customerId=source.customerId;
        target.type=source.type;
        target.number=source.number;
        target.status=source.status;
        target.balance=source.balance;
        target.availableBalance=source.availableBalance;
        target.gmfExempt=source.gmfExempt;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.version=source.version;
        return target;
    }
    private static com.nexo.banking.entity.Account toEntity(Account source) {
        var target=new com.nexo.banking.entity.Account();
        target.id=source.id;
        target.customerId=source.customerId;
        target.type=source.type;
        target.number=source.number;
        target.status=source.status;
        target.balance=source.balance;
        target.availableBalance=source.availableBalance;
        target.gmfExempt=source.gmfExempt;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.version=source.version;
        return target;
    }
}
