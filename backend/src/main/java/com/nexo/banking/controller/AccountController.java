package com.nexo.banking.controller;
import com.nexo.banking.domain.model.Account;
import com.nexo.banking.dto.Requests.*;
import com.nexo.banking.application.port.in.AccountUseCases;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/accounts")
public class AccountController {
    private final AccountUseCases service;
    public AccountController(AccountUseCases service) { this.service=service; }
    @GetMapping public List<Account> list(@RequestParam(required=false) Long customerId) { return service.list(customerId); }
    @GetMapping("/{id}") public Account get(@PathVariable Long id) { return service.get(id); }
    @GetMapping("/{id}/statement") public AccountUseCases.Statement statement(@PathVariable Long id) { return service.statement(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Account create(@Valid @RequestBody AccountInput in) { return service.create(in.toCommand()); }
    @PutMapping("/{id}") public Account update(@PathVariable Long id,@Valid @RequestBody AccountUpdate in) { return service.update(id,in.toCommand()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
