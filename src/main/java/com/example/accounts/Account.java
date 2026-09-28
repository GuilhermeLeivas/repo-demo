package com.example.accounts;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(name = "uk_account_document", columnNames = "document"))
class Account {
    @Id UUID id;
    @Version Long version;
    @Column(nullable = false, length = 100) String holder;
    @Column(nullable = false, length = 11, updatable = false) String document;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal balance;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal dailyLimit;
    @Column(nullable = false, precision = 19, scale = 2) BigDecimal dailySpent;
    @Column(nullable = false) LocalDate spendingDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Status status;
    protected Account() {}
    enum Status { ACTIVE, BLOCKED }
}
