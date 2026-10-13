package com.nexo.banking.config;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
import com.nexo.banking.application.port.in.CustomerUseCases;
import org.springframework.transaction.annotation.*;

/** Decorator applies ACID boundaries without coupling the use case to Spring. */
@Transactional(readOnly=true)
public class TransactionalCustomerUseCases implements CustomerUseCases {
    private final CustomerUseCases delegate;
    public TransactionalCustomerUseCases(CustomerUseCases delegate) { this.delegate=delegate; }
    @Override public List<Customer> list() { return delegate.list(); }
    @Override public Customer get(Long id) { return delegate.get(id); }
    @Override @Transactional public Customer create(CustomerInput in) { return delegate.create(in); }
    @Override @Transactional public Customer update(Long id,CustomerInput in) { return delegate.update(id,in); }
    @Override @Transactional public void delete(Long id) { delegate.delete(id); }
}
