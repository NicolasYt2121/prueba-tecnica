package com.nexo.banking.repository;
import com.nexo.banking.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByCustomerId(Long customerId);
    List<Account> findByCustomerIdOrderByIdDesc(Long customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.id=:id")
    Optional<Account> lockById(@Param("id") Long id);
}
