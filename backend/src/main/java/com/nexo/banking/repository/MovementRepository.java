package com.nexo.banking.repository;
import com.nexo.banking.entity.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MovementRepository extends JpaRepository<Movement,Long> {
    List<Movement> findByAccountIdOrderByIdAsc(Long accountId);
}
