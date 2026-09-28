package com.example.accounts;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

final class Contracts {
    private Contracts() {}
    record CreateAccount(
        @NotBlank @Size(max = 100) String holder,
        @NotBlank @Pattern(regexp = "[0-9]{11}") String document,
        @NotNull @DecimalMin("0.01") @DecimalMax("10000.00") @Digits(integer = 5, fraction = 2) BigDecimal dailyLimit) {}
    record UpdateAccount(
        @NotBlank @Size(max = 100) String holder,
        @NotNull @DecimalMin("0.01") @DecimalMax("10000.00") @Digits(integer = 5, fraction = 2) BigDecimal dailyLimit,
        @NotNull Account.Status status) {}
    record AccountSummary(UUID id, BigDecimal balance, BigDecimal dailyLimit, Account.Status status) {}
    record Movement(
        @NotNull @DecimalMin("0.01") @DecimalMax("100000.00") @Digits(integer = 6, fraction = 2) BigDecimal amount) {}
    record AccountView(UUID id, String holder, String maskedDocument, BigDecimal balance,
                       BigDecimal dailyLimit, Account.Status status) {
        static AccountView of(Account a) {
            return new AccountView(a.id, a.holder, "*******" + a.document.substring(7), a.balance, a.dailyLimit, a.status);
        }
    }
}
