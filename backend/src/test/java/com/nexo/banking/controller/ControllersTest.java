package com.nexo.banking.controller;
import com.nexo.banking.service.*;
import com.nexo.banking.entity.*;
import com.nexo.banking.dto.Requests.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.*;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest({CustomerController.class,AccountController.class,TransactionController.class})
class ControllersTest {
    @Autowired MockMvc mvc;
    @MockitoBean CustomerService customers; @MockitoBean AccountService accounts; @MockitoBean TransactionService transactions;
    final String customer="""
      {"identificationType":"CC","identificationNumber":"123456","firstName":"Ana","lastName":"Perez","email":"ana@example.com","birthDate":"1990-01-01"}
      """;
    final String transaction="""
      {"requestId":"96c67d55-a650-480a-9331-a6c2a51df519","type":"CONSIGNACION","destinationAccountId":1,"amount":10.25,"description":"Prueba"}
      """;
    @Test void customerCrud() throws Exception {Customer c=new Customer();c.id=1L;when(customers.create(any())).thenReturn(c);when(customers.update(eq(1L),any())).thenReturn(c);when(customers.get(1L)).thenReturn(c);when(customers.list()).thenReturn(List.of(c));mvc.perform(post("/api/customers").contentType("application/json").content(customer)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));mvc.perform(put("/api/customers/1").contentType("application/json").content(customer)).andExpect(status().isOk());mvc.perform(get("/api/customers/1")).andExpect(status().isOk());mvc.perform(get("/api/customers")).andExpect(jsonPath("$[0].id").value(1));mvc.perform(delete("/api/customers/1")).andExpect(status().isNoContent());verify(customers).delete(1L);}
    @Test void accountCrudAndStatement() throws Exception {Account a=new Account();a.id=2L;when(accounts.create(any())).thenReturn(a);when(accounts.update(eq(2L),any())).thenReturn(a);when(accounts.get(2L)).thenReturn(a);when(accounts.list(1L)).thenReturn(List.of(a));when(accounts.statement(2L)).thenReturn(new AccountService.Statement(a,List.of()));mvc.perform(post("/api/accounts").contentType("application/json").content("{\"customerId\":1,\"type\":\"AHORROS\",\"gmfExempt\":true}")).andExpect(status().isCreated());mvc.perform(put("/api/accounts/2").contentType("application/json").content("{\"status\":\"INACTIVA\",\"gmfExempt\":false}")).andExpect(status().isOk());mvc.perform(get("/api/accounts?customerId=1")).andExpect(jsonPath("$[0].id").value(2));mvc.perform(get("/api/accounts/2")).andExpect(status().isOk());mvc.perform(get("/api/accounts/2/statement")).andExpect(jsonPath("$.movements").isArray());mvc.perform(delete("/api/accounts/2")).andExpect(status().isNoContent());}
    @Test void transactionCrud() throws Exception {FinancialTransaction t=new FinancialTransaction();t.id=3L;when(transactions.create(any())).thenReturn(t);when(transactions.update(eq(3L),any())).thenReturn(t);when(transactions.get(3L)).thenReturn(t);when(transactions.list()).thenReturn(List.of(t));mvc.perform(post("/api/transactions").contentType("application/json").content(transaction)).andExpect(status().isCreated());mvc.perform(put("/api/transactions/3").contentType("application/json").content("{\"description\":\"Nueva nota\"}")).andExpect(status().isOk());mvc.perform(get("/api/transactions/3")).andExpect(status().isOk());mvc.perform(get("/api/transactions")).andExpect(jsonPath("$[0].id").value(3));mvc.perform(delete("/api/transactions/3")).andExpect(status().isNoContent());}
    @Test void rejectsBadEmailAndShortName() throws Exception {mvc.perform(post("/api/customers").contentType("application/json").content(customer.replace("ana@example.com","invalid").replace("Ana","A"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.email").exists()).andExpect(jsonPath("$.fields.firstName").exists());verifyNoInteractions(customers);}
    @Test void rejectsBadMoneyAndNullRequiredFields() throws Exception {mvc.perform(post("/api/transactions").contentType("application/json").content(transaction.replace("10.25","-1"))).andExpect(status().isBadRequest());mvc.perform(post("/api/accounts").contentType("application/json").content("{}")).andExpect(status().isBadRequest());}
    @Test void rejectsUnknownEnumsMalformedJsonAndMassAssignment() throws Exception {for(String body:List.of("{",customer.replace("CC","TI"),customer.replace("\"firstName\"","\"balance\"")))mvc.perform(post("/api/customers").contentType("application/json").content(body)).andExpect(status().isBadRequest());mvc.perform(get("/api/accounts/not-an-id")).andExpect(status().isBadRequest());}
    @Test void businessErrorsUseProblemDetails() throws Exception {when(customers.get(99L)).thenThrow(BusinessException.notFound("Cliente"));mvc.perform(get("/api/customers/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Cliente no encontrado"));doThrow(BusinessException.conflict("Tiene productos")).when(customers).delete(1L);mvc.perform(delete("/api/customers/1")).andExpect(status().isConflict());}
    @Test void integrityConflictsDoNotLeakSql() throws Exception {when(customers.create(any())).thenThrow(new DataIntegrityViolationException("secret SQL"));mvc.perform(post("/api/customers").contentType("application/json").content(customer)).andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret"))));}
    @Test void concurrentConflictHasRetryGuidance() throws Exception {when(accounts.get(1L)).thenThrow(new ObjectOptimisticLockingFailureExceptionForTest());mvc.perform(get("/api/accounts/1")).andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("misma clave")));}
    static class ObjectOptimisticLockingFailureExceptionForTest extends OptimisticLockingFailureException { ObjectOptimisticLockingFailureExceptionForTest(){super("concurrent");} }
}
