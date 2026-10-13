package com.nexo.banking.application.port.out;
import com.nexo.banking.domain.model.*;
import java.util.*;
/** Persistence contract; locking operations require an enclosing transaction. */
public interface TransactionRepository {
    List<FinancialTransaction> findAll();
    Optional<FinancialTransaction> findById(Long id);
    Optional<FinancialTransaction> lockById(Long id);
    Optional<FinancialTransaction> findByRequestId(UUID id);
    FinancialTransaction save(FinancialTransaction value);
    FinancialTransaction saveAndFlush(FinancialTransaction value);
}
