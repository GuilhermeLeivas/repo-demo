package com.example.accounts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.example.accounts.Contracts.*;

@RestController
@RequestMapping("/api/accounts")
class AccountController {
    private final AccountService service;
    AccountController(AccountService service) { this.service = service; }
    @GetMapping
    Page<AccountView> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                           @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(page, size);
    }
    @GetMapping("/{id}") AccountView get(@PathVariable UUID id) { return service.get(id); }
    @PostMapping ResponseEntity<AccountView> create(@Valid @RequestBody CreateAccount body) {
        var account = service.create(body);
        return ResponseEntity.created(URI.create("/api/accounts/" + account.id())).body(account);
    }
    @PutMapping("/{id}") AccountView update(@PathVariable UUID id, @Valid @RequestBody UpdateAccount body) {
        return service.update(id, body);
    }
    @DeleteMapping("/{id}") ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/credits") AccountView credit(@PathVariable UUID id, @Valid @RequestBody Movement body) {
        return service.credit(id, body);
    }
    @PostMapping("/{id}/debits") AccountView debit(@PathVariable UUID id, @Valid @RequestBody Movement body) {
        return service.debit(id, body);
    }
}
