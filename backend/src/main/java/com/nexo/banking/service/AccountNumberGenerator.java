package com.nexo.banking.service;
import com.nexo.banking.entity.Types.AccountType;
public interface AccountNumberGenerator { String next(AccountType type); }
