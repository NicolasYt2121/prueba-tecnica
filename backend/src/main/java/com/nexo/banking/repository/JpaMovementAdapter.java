package com.nexo.banking.repository;
import com.nexo.banking.domain.model.Movement;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

@Component
public class JpaMovementAdapter implements com.nexo.banking.application.port.out.MovementRepository {
    private final MovementRepository repository;
    public JpaMovementAdapter(MovementRepository repository) { this.repository=repository; }
    @Override public List<Movement> findByAccountIdOrderByIdAsc(Long id) { return repository.findByAccountIdOrderByIdAsc(id).stream().map(JpaMovementAdapter::toDomain).toList(); }
    @Override public Movement save(Movement value) {
        var saved=repository.save(toEntity(value));
        value.id=saved.id;
        return toDomain(saved);
    }
    private static Movement toDomain(com.nexo.banking.entity.Movement source) {
        var target=new Movement();
        target.id=source.id;
        target.transactionId=source.transactionId;
        target.accountId=source.accountId;
        target.delta=source.delta;
        target.balanceAfter=source.balanceAfter;
        target.reversal=source.reversal;
        target.createdAt=source.createdAt;
        return target;
    }
    private static com.nexo.banking.entity.Movement toEntity(Movement source) {
        var target=new com.nexo.banking.entity.Movement();
        target.id=source.id;
        target.transactionId=source.transactionId;
        target.accountId=source.accountId;
        target.delta=source.delta;
        target.balanceAfter=source.balanceAfter;
        target.reversal=source.reversal;
        target.createdAt=source.createdAt;
        return target;
    }
}
