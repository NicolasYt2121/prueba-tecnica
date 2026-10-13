package com.nexo.banking.service;
import com.nexo.banking.application.port.in.AccountUseCases;
import com.nexo.banking.domain.model.*;
import com.nexo.banking.domain.model.Types.*;
import com.nexo.banking.application.Commands.*;
import com.nexo.banking.application.port.out.*;
import java.time.Clock;
import java.util.List;
public class AccountService implements AccountUseCases {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final MovementRepository movements;
    private final AccountNumberGenerator numbers;
    private final Clock clock;
    public AccountService(AccountRepository accounts,CustomerRepository customers,MovementRepository movements,AccountNumberGenerator numbers,Clock clock) {
        this.accounts=accounts; this.customers=customers; this.movements=movements; this.numbers=numbers; this.clock=clock;
    }
    public List<Account> list(Long customerId) { return customerId==null ? accounts.findAll() : accounts.findByCustomerIdOrderByIdDesc(customerId); }
    public Account get(Long id) { return accounts.findById(id).orElseThrow(()->BusinessException.notFound("Cuenta")); }
    public Account create(AccountInput in) {
        customers.lockById(in.customerId()).orElseThrow(()->BusinessException.notFound("Cliente"));
        Account a=new Account(); a.customerId=in.customerId(); a.type=in.type(); a.number=numbers.next(in.type());
        a.status=AccountStatus.ACTIVA; a.gmfExempt=in.gmfExempt(); a.createdAt=clock.instant(); a.updatedAt=a.createdAt;
        return accounts.save(a);
    }
    public Account update(Long id,AccountUpdate in) {
        Account a=accounts.lockById(id).orElseThrow(()->BusinessException.notFound("Cuenta"));
        if(a.status==AccountStatus.CANCELADA && in.status()!=AccountStatus.CANCELADA) throw BusinessException.conflict("Una cuenta cancelada no puede reabrirse");
        if(in.status()==AccountStatus.CANCELADA && a.balance.signum()!=0) throw BusinessException.conflict("Solo se puede cancelar una cuenta con saldo cero");
        a.status=in.status(); a.gmfExempt=in.gmfExempt(); a.updatedAt=clock.instant(); return accounts.save(a);
    }
    public void delete(Long id) {
        Account a=accounts.lockById(id).orElseThrow(()->BusinessException.notFound("Cuenta"));
        update(id,new AccountUpdate(AccountStatus.CANCELADA,a.gmfExempt));
    }
    public Statement statement(Long id) { return new Statement(get(id),movements.findByAccountIdOrderByIdAsc(id)); }
}
