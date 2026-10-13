package com.nexo.banking.config;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
import com.nexo.banking.application.port.in.AccountUseCases;
import org.springframework.transaction.annotation.*;

/** Decorator applies ACID boundaries without coupling the use case to Spring. */
@Transactional(readOnly=true)
public class TransactionalAccountUseCases implements AccountUseCases {
    private final AccountUseCases delegate;
    public TransactionalAccountUseCases(AccountUseCases delegate) { this.delegate=delegate; }
    @Override public List<Account> list(Long customerId) { return delegate.list(customerId); }
    @Override public Account get(Long id) { return delegate.get(id); }
    @Override @Transactional public Account create(AccountInput in) { return delegate.create(in); }
    @Override @Transactional public Account update(Long id,AccountUpdate in) { return delegate.update(id,in); }
    @Override @Transactional public void delete(Long id) { delegate.delete(id); }
    @Override @Transactional(isolation=Isolation.REPEATABLE_READ,readOnly=true) public Statement statement(Long id) { return delegate.statement(id); }
}
