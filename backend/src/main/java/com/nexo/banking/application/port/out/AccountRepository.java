package com.nexo.banking.application.port.out;
import com.nexo.banking.domain.model.*;
import java.util.*;
/** Persistence contract; locking operations require an enclosing transaction. */
public interface AccountRepository {
    List<Account> findAll();
    Optional<Account> findById(Long id);
    Optional<Account> lockById(Long id);
    Account save(Account value);
    boolean existsByCustomerId(Long id);
    List<Account> findByCustomerIdOrderByIdDesc(Long id);
}
