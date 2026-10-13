package com.nexo.banking.controller;
import com.nexo.banking.entity.FinancialTransaction;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;
    public TransactionController(TransactionService service) { this.service=service; }
    @GetMapping public List<FinancialTransaction> list() { return service.list(); }
    @GetMapping("/{id}") public FinancialTransaction get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public FinancialTransaction create(@Valid @RequestBody TransactionInput in) { return service.create(in); }
    @PutMapping("/{id}") public FinancialTransaction update(@PathVariable Long id,@Valid @RequestBody TransactionUpdate in) { return service.update(id,in); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
