package com.nexo.banking.repository;
import com.nexo.banking.domain.model.FinancialTransaction;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

@Component
public class JpaFinancialTransactionAdapter implements com.nexo.banking.application.port.out.TransactionRepository {
    private final TransactionRepository repository;
    public JpaFinancialTransactionAdapter(TransactionRepository repository) { this.repository=repository; }
    @Override public List<FinancialTransaction> findAll() { return repository.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().map(JpaFinancialTransactionAdapter::toDomain).toList(); }
    @Override public Optional<FinancialTransaction> findById(Long id) { return repository.findById(id).map(JpaFinancialTransactionAdapter::toDomain); }
    @Override public Optional<FinancialTransaction> lockById(Long id) { return repository.lockById(id).map(JpaFinancialTransactionAdapter::toDomain); }
    @Override public Optional<FinancialTransaction> findByRequestId(UUID id) { return repository.findByRequestId(id).map(JpaFinancialTransactionAdapter::toDomain); }
    @Override public FinancialTransaction save(FinancialTransaction value) {
        var saved=repository.save(toEntity(value));
        value.id=saved.id;
        value.version=saved.version;
        return toDomain(saved);
    }
    @Override public FinancialTransaction saveAndFlush(FinancialTransaction value) {
        var saved=repository.saveAndFlush(toEntity(value));
        value.id=saved.id;
        value.version=saved.version;
        return toDomain(saved);
    }
    private static FinancialTransaction toDomain(com.nexo.banking.entity.FinancialTransaction source) {
        var target=new FinancialTransaction();
        target.id=source.id;
        target.requestId=source.requestId;
        target.type=source.type;
        target.sourceAccountId=source.sourceAccountId;
        target.destinationAccountId=source.destinationAccountId;
        target.amount=source.amount;
        target.description=source.description;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.reversed=source.reversed;
        target.reversedAt=source.reversedAt;
        target.version=source.version;
        return target;
    }
    private static com.nexo.banking.entity.FinancialTransaction toEntity(FinancialTransaction source) {
        var target=new com.nexo.banking.entity.FinancialTransaction();
        target.id=source.id;
        target.requestId=source.requestId;
        target.type=source.type;
        target.sourceAccountId=source.sourceAccountId;
        target.destinationAccountId=source.destinationAccountId;
        target.amount=source.amount;
        target.description=source.description;
        target.createdAt=source.createdAt;
        target.updatedAt=source.updatedAt;
        target.reversed=source.reversed;
        target.reversedAt=source.reversedAt;
        target.version=source.version;
        return target;
    }
}
