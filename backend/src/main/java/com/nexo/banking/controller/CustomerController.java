package com.nexo.banking.controller;
import com.nexo.banking.domain.model.Customer;
import com.nexo.banking.dto.Requests.CustomerInput;
import com.nexo.banking.application.port.in.CustomerUseCases;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerUseCases service;
    public CustomerController(CustomerUseCases service) { this.service=service; }
    @GetMapping public List<Customer> list() { return service.list(); }
    @GetMapping("/{id}") public Customer get(@PathVariable Long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Customer create(@Valid @RequestBody CustomerInput in) { return service.create(in.toCommand()); }
    @PutMapping("/{id}") public Customer update(@PathVariable Long id,@Valid @RequestBody CustomerInput in) { return service.update(id,in.toCommand()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
