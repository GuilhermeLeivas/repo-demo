package com.example.accounts;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiErrors {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<?> business(BusinessException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("code", e.code, "message", e.getMessage()));
    }
    @ExceptionHandler(AccountService.MissingAccountException.class)
    ResponseEntity<?> missing() { return ResponseEntity.status(404).body(Map.of("code", "ACCOUNT_NOT_FOUND")); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> duplicate() { return ResponseEntity.status(409).body(Map.of("code", "DATA_CONFLICT")); }
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<?> concurrency() { return ResponseEntity.status(409).body(Map.of("code", "CONCURRENT_CHANGE", "message", "Releia a conta antes de tentar novamente.")); }
}
