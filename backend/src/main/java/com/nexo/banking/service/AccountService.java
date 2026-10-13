package com.nexo.banking.service;
import com.nexo.banking.entity.*;
import com.nexo.banking.entity.Types.*;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.repository.*;
import java.time.Clock;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @Transactional(readOnly=true)
public class AccountService {
    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final MovementRepository movements;
    private final AccountNumberGenerator numbers;
    private final Clock clock;
    public AccountService(AccountRepository accounts,CustomerRepository customers,MovementRepository movements,AccountNumberGenerator numbers,Clock clock) {
        this.accounts=accounts; this.customers=customers; this.movements=movements; this.numbers=numbers; this.clock=clock;
    }
    public List<Account> list(Long customerId) { return customerId==null ? accounts.findAll(Sort.by(Sort.Direction.DESC,"id")) : accounts.findByCustomerIdOrderByIdDesc(customerId); }
    public Account get(Long id) { return accounts.findById(id).orElseThrow(()->BusinessException.notFound("Cuenta")); }
    @Transactional public Account create(AccountInput in) {
        customers.lockById(in.customerId()).orElseThrow(()->BusinessException.notFound("Cliente"));
        Account a=new Account(); a.customerId=in.customerId(); a.type=in.type(); a.number=numbers.next(in.type());
        a.status=AccountStatus.ACTIVA; a.gmfExempt=in.gmfExempt(); a.createdAt=clock.instant(); a.updatedAt=a.createdAt;
        return accounts.save(a);
    }
    @Transactional public Account update(Long id,AccountUpdate in) {
        Account a=accounts.lockById(id).orElseThrow(()->BusinessException.notFound("Cuenta"));
        if(a.status==AccountStatus.CANCELADA && in.status()!=AccountStatus.CANCELADA) throw BusinessException.conflict("Una cuenta cancelada no puede reabrirse");
        if(in.status()==AccountStatus.CANCELADA && a.balance.signum()!=0) throw BusinessException.conflict("Solo se puede cancelar una cuenta con saldo cero");
        a.status=in.status(); a.gmfExempt=in.gmfExempt(); a.updatedAt=clock.instant(); return accounts.save(a);
    }
    @Transactional public void delete(Long id) {
        Account a=accounts.lockById(id).orElseThrow(()->BusinessException.notFound("Cuenta"));
        update(id,new AccountUpdate(AccountStatus.CANCELADA,a.gmfExempt));
    }
    public record Statement(Account account,List<Movement> movements) {}
    @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
    public Statement statement(Long id) { return new Statement(get(id),movements.findByAccountIdOrderByIdAsc(id)); }
}
