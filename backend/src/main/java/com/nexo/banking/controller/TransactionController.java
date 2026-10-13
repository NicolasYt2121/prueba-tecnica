package com.nexo.banking.controller;
import com.nexo.banking.domain.model.FinancialTransaction;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.application.port.in.TransactionUseCases;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionUseCases service;
    public TransactionController(TransactionUseCases service) { this.service=service; }
    @GetMapping public List<FinancialTransaction> list() { return service.list(); }
    @GetMapping("/{id}") public FinancialTransaction get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public FinancialTransaction create(@Valid @RequestBody TransactionInput in) { return service.create(in.toCommand()); }
    @PutMapping("/{id}") public FinancialTransaction update(@PathVariable Long id,@Valid @RequestBody TransactionUpdate in) { return service.update(id,in.toCommand()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
