package com.nexo.banking.application.port.out;
import com.nexo.banking.domain.model.*;
import java.util.*;
/** Persistence contract; locking operations require an enclosing transaction. */
public interface CustomerRepository {
    List<Customer> findAll();
    Optional<Customer> findById(Long id);
    Optional<Customer> lockById(Long id);
    Customer save(Customer value);
    void delete(Customer value);
}
