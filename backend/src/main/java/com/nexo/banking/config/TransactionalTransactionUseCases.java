package com.nexo.banking.config;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
import com.nexo.banking.application.port.in.TransactionUseCases;
import org.springframework.transaction.annotation.*;

/** Decorator applies ACID boundaries without coupling the use case to Spring. */
@Transactional(readOnly=true)
public class TransactionalTransactionUseCases implements TransactionUseCases {
    private final TransactionUseCases delegate;
    public TransactionalTransactionUseCases(TransactionUseCases delegate) { this.delegate=delegate; }
    @Override public List<FinancialTransaction> list() { return delegate.list(); }
    @Override public FinancialTransaction get(Long id) { return delegate.get(id); }
    @Override @Transactional public FinancialTransaction create(TransactionInput in) { return delegate.create(in); }
    @Override @Transactional public FinancialTransaction update(Long id,TransactionUpdate in) { return delegate.update(id,in); }
    @Override @Transactional public void delete(Long id) { delegate.delete(id); }
}
