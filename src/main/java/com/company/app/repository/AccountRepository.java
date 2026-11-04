package com.company.app.repository;

import com.company.app.domain.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(String id);

    void save(Account account);

    void clear();
}
