package com.example.accounts;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.accounts.Contracts.*;

@Service
@Transactional(readOnly = true)
class AccountService {
    private final AccountRepository accounts;
    private final Clock clock;
    AccountService(AccountRepository accounts, Clock clock) { this.accounts = accounts; this.clock = clock; }

    Page<AccountView> list(int page, int size) {
        return accounts.findAll(PageRequest.of(page, size, Sort.by("id"))).map(AccountView::of);
    }
    AccountView get(UUID id) { return AccountView.of(required(id)); }

    @Transactional
    public AccountView create(CreateAccount request) {
        if (accounts.existsByDocument(request.document()))
            throw new BusinessException("DUPLICATE_DOCUMENT", "Já existe uma conta com esse documento.");
        var a = new Account();
        a.id = UUID.randomUUID();
        a.holder = request.holder().strip();
        a.document = request.document();
        a.balance = new BigDecimal("0.00");
        a.dailyLimit = request.dailyLimit();
        a.dailySpent = new BigDecimal("0.00");
        a.spendingDate = LocalDate.now(clock);
        a.status = Account.Status.ACTIVE;
        return AccountView.of(accounts.saveAndFlush(a));
    }

    @Transactional
    public AccountView update(UUID id, UpdateAccount request) {
        var a = required(id);
        a.holder = request.holder().strip();
        a.dailyLimit = request.dailyLimit();
        a.status = request.status();
        return AccountView.of(accounts.saveAndFlush(a));
    }

    @Transactional
    public void delete(UUID id) {
        var a = required(id);
        if (a.balance.signum() != 0)
            throw new BusinessException("NON_ZERO_BALANCE", "A conta deve estar zerada para exclusão.");
        accounts.delete(a);
        accounts.flush();
    }

    @Transactional
    public AccountView credit(UUID id, Movement request) {
        var a = active(id);
        var balance = a.balance.add(request.amount());
        if (balance.compareTo(new BigDecimal("1000000.00")) > 0)
            throw new BusinessException("BALANCE_LIMIT", "Saldo máximo da demo excedido.");
        a.balance = balance;
        return AccountView.of(accounts.saveAndFlush(a));
    }

    @Transactional
    public AccountView debit(UUID id, Movement request) {
        var a = active(id);
        var today = LocalDate.now(clock);
        var spent = today.equals(a.spendingDate) ? a.dailySpent : new BigDecimal("0.00");
        a.balance = a.balance.subtract(request.amount());
        a.dailySpent = spent.add(request.amount());
        a.spendingDate = today;
        return AccountView.of(accounts.saveAndFlush(a));
    }

    private Account active(UUID id) {
        var a = required(id);
        if (a.status != Account.Status.ACTIVE)
            throw new BusinessException("BLOCKED_ACCOUNT", "Conta bloqueada não pode movimentar saldo.");
        return a;
    }
    private Account required(UUID id) {
        return accounts.findById(id).orElseThrow(() -> new MissingAccountException());
    }
    static class MissingAccountException extends RuntimeException {}
}
