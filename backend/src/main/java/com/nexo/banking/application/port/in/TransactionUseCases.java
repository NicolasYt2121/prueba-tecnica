package com.nexo.banking.application.port.in;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
public interface TransactionUseCases {
    List<FinancialTransaction> list();
    FinancialTransaction get(Long id);
    FinancialTransaction create(TransactionInput in);
    FinancialTransaction update(Long id,TransactionUpdate in);
    void delete(Long id);
}
