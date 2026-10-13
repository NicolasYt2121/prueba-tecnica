package com.nexo.banking.controller;
import com.nexo.banking.service.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.util.Map;
import java.util.stream.Collectors;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BusinessException.class) public ResponseEntity<ProblemDetail> business(BusinessException ex) { return problem(switch(ex.code) { case NOT_FOUND -> 404; case CONFLICT -> 409; case INVALID -> 422; },ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        ProblemDetail p=ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Revisa los campos del formulario");
        Map<String,String> fields=ex.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(e->e.getField(),e->e.getDefaultMessage(),(a,b)->a));
        p.setProperty("fields",fields); return ResponseEntity.badRequest().body(p);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,ConstraintViolationException.class})
    public ResponseEntity<ProblemDetail> malformed(Exception ex) { return problem(400,"El contenido, tipo o formato de los datos no es vÃ¡lido"); }
    @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<ProblemDetail> integrity(DataIntegrityViolationException ex) {
        return problem(409,"Conflicto de integridad: identificaciÃ³n o solicitud duplicada, saldo invÃ¡lido o registro vinculado. Consulta el estado antes de reintentar.");
    }
    @ExceptionHandler({PessimisticLockingFailureException.class,OptimisticLockingFailureException.class})
    public ResponseEntity<ProblemDetail> concurrency(Exception ex) { return problem(409,"Otro proceso modificÃ³ el registro. Consulta el estado y reintenta con la misma clave de solicitud."); }
    private ResponseEntity<ProblemDetail> problem(int status,String detail) { return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),detail)); }
}
