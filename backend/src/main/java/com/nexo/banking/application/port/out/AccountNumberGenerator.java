package com.nexo.banking.application.port.out;
import com.nexo.banking.domain.model.Types.AccountType;
public interface AccountNumberGenerator { String next(AccountType type); }
