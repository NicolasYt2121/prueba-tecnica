package com.nexo.banking.application.port.out;
import com.nexo.banking.domain.model.*;
import java.util.*;
/** Persistence contract; locking operations require an enclosing transaction. */
public interface MovementRepository {
    List<Movement> findByAccountIdOrderByIdAsc(Long id);
    Movement save(Movement value);
}
