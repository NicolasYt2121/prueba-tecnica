package com.nexo.banking.service;
import com.nexo.banking.entity.*;
import com.nexo.banking.entity.Types.*;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.repository.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import java.util.stream.Stream;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class TransactionService {
    private final TransactionRepository transactions;
    private final AccountRepository accounts;
    private final MovementRepository movements;
    private final Clock clock;
    public TransactionService(TransactionRepository transactions,AccountRepository accounts,MovementRepository movements,Clock clock) {
        this.transactions=transactions; this.accounts=accounts; this.movements=movements; this.clock=clock;
    }
    public List<FinancialTransaction> list() { return transactions.findAll(Sort.by(Sort.Direction.DESC,"id")); }
    public FinancialTransaction get(Long id) { return transactions.findById(id).orElseThrow(()->BusinessException.notFound("Transacción")); }
    @Transactional public FinancialTransaction create(TransactionInput in) {
        validate(in);
        Optional<FinancialTransaction> previous=transactions.findByRequestId(in.requestId());
        if(previous.isPresent()) {
            FinancialTransaction p=previous.get();
            if(p.type!=in.type() || !Objects.equals(p.sourceAccountId,in.sourceAccountId()) || !Objects.equals(p.destinationAccountId,in.destinationAccountId()) || p.amount.compareTo(in.amount())!=0)
                throw BusinessException.conflict("La clave de idempotencia ya se utilizó con otros datos");
            return p;
        }
        Map<Long,Account> locked=lock(in.sourceAccountId(),in.destinationAccountId());
        FinancialTransaction t=new FinancialTransaction(); t.requestId=in.requestId(); t.type=in.type(); t.amount=in.amount();
        t.sourceAccountId=in.sourceAccountId(); t.destinationAccountId=in.destinationAccountId(); t.description=in.description().strip();
        t.createdAt=clock.instant(); t.updatedAt=t.createdAt;
        // The unique request key is flushed before any balance changes; a racing duplicate rolls back.
        transactions.saveAndFlush(t);
        apply(t,locked,false); return t;
    }
    @Transactional public FinancialTransaction update(Long id,TransactionUpdate in) {
        FinancialTransaction t=transactions.lockById(id).orElseThrow(()->BusinessException.notFound("Transacción"));
        if(t.reversed) throw BusinessException.conflict("La transacción está anulada");
        t.description=in.description().strip(); t.updatedAt=clock.instant(); return transactions.save(t);
    }
    @Transactional public void delete(Long id) {
        FinancialTransaction t=transactions.lockById(id).orElseThrow(()->BusinessException.notFound("Transacción"));
        if(t.reversed) return;
        Map<Long,Account> locked=lock(t.sourceAccountId,t.destinationAccountId);
        apply(t,locked,true); t.reversed=true; t.reversedAt=clock.instant(); t.updatedAt=t.reversedAt; transactions.save(t);
    }
    private void validate(TransactionInput in) {
        if(in.amount()==null || in.amount().signum()<=0 || in.amount().scale()>2 || in.amount().compareTo(new BigDecimal("99999999999999999.99"))>0) throw BusinessException.invalid("Importe positivo, máximo 17 enteros y 2 decimales");
        boolean source=in.sourceAccountId()!=null, destination=in.destinationAccountId()!=null;
        boolean valid=switch(in.type()) {
            case CONSIGNACION -> !source && destination;
            case RETIRO -> source && !destination;
            case TRANSFERENCIA -> source && destination && !in.sourceAccountId().equals(in.destinationAccountId());
        };
        if(!valid) throw BusinessException.invalid("Cuentas incompatibles con el tipo de transacción");
    }
    private Map<Long,Account> lock(Long source,Long destination) {
        Map<Long,Account> result=new LinkedHashMap<>();
        Stream.of(source,destination).filter(Objects::nonNull).distinct().sorted().forEach(id->{
            Account a=accounts.lockById(id).orElseThrow(()->BusinessException.notFound("Cuenta"));
            if(a.status!=AccountStatus.ACTIVA) throw BusinessException.conflict("La cuenta "+a.number+" no está activa");
            result.put(id,a);
        }); return result;
    }
    private void apply(FinancialTransaction t,Map<Long,Account> locked,boolean reversal) {
        Map<Long,BigDecimal> deltas=new LinkedHashMap<>();
        if(t.sourceAccountId!=null) deltas.put(t.sourceAccountId,reversal ? t.amount : t.amount.negate());
        if(t.destinationAccountId!=null) deltas.put(t.destinationAccountId,reversal ? t.amount.negate() : t.amount);
        // Validate every resulting balance before mutating either account.
        deltas.forEach((id,delta)->{
            Account a=locked.get(id); BigDecimal next=a.balance.add(delta);
            if(a.type==AccountType.AHORROS && next.signum()<0) throw BusinessException.conflict("Fondos insuficientes en la cuenta de ahorros "+a.number);
            if(next.abs().compareTo(new BigDecimal("99999999999999999.99"))>0) throw BusinessException.conflict("El saldo supera el límite permitido");
        });
        deltas.forEach((id,delta)->{
            Account a=locked.get(id); a.balance=a.balance.add(delta); a.availableBalance=a.balance; a.updatedAt=clock.instant(); accounts.save(a);
            Movement m=new Movement(); m.transactionId=t.id; m.accountId=id; m.delta=delta; m.balanceAfter=a.balance;
            m.reversal=reversal; m.createdAt=clock.instant(); movements.save(m);
        });
    }
}
