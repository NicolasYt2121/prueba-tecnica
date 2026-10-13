package com.nexo.banking.application.port.in;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.application.Commands.*;
import java.util.List;
public interface CustomerUseCases {
    List<Customer> list();
    Customer get(Long id);
    Customer create(CustomerInput in);
    Customer update(Long id,CustomerInput in);
    void delete(Long id);
}
