package com.nexo.banking.repository;
import com.nexo.banking.entity.FinancialTransaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface TransactionRepository extends JpaRepository<FinancialTransaction,Long> {
    Optional<FinancialTransaction> findByRequestId(UUID requestId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select t from FinancialTransaction t where t.id=:id")
    Optional<FinancialTransaction> lockById(@Param("id") Long id);
}
