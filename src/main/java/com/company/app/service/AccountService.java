package com.company.app.service;

import com.company.app.domain.Account;
import com.company.app.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account deposit(String accountId, BigDecimal amount) {

        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID must not be empty");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = accountRepository.findById(accountId)
                .orElseGet(() -> new Account(accountId, BigDecimal.ZERO));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        return account;
    }

    public Optional<Account> getBalance(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID must not be empty");
        }

        return accountRepository.findById(accountId);
    }
}
