package com.example.accounts;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByDocument(String document);
}
