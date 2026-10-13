package com.nexo.banking.repository;
import com.nexo.banking.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface CustomerRepository extends JpaRepository<Customer,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Customer c where c.id=:id")
    Optional<Customer> lockById(@Param("id") Long id);
}
