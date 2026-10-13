package com.nexo.banking.application.port.in;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
public interface AccountUseCases {
    record Statement(Account account,List<Movement> movements) {}
    List<Account> list(Long customerId);
    Account get(Long id);
    Account create(AccountInput in);
    Account update(Long id,AccountUpdate in);
    void delete(Long id);
    Statement statement(Long id);
}
