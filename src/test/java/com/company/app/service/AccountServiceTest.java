package com.company.app.service;

import com.company.app.domain.Account;
import com.company.app.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountServiceTest {
    @Test
    void shouldCreateAccountWhenDepositingIntoNonExistingAccount() {
        //arange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);

        //act
        Account account = service.deposit("123", new BigDecimal("10.00"));

        //assert
        assertEquals("123", account.getId());
        assertEquals(0, new BigDecimal("10.00").compareTo(account.getBalance()));

        //validate actual repository state
        Account storedAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(storedAccount.getBalance()));

    }

    @Test
    void shouldAccumulateDepositsIntoExistingAccount() {
        //arange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);

        //act
        service.deposit("123", new BigDecimal("10.00"));
        Account account = service.deposit("123", new BigDecimal("5.00"));

        //assert
        assertEquals("123", account.getId());
        assertEquals(0, new BigDecimal("15.00").compareTo(account.getBalance()));

        //validate actual repository state
        Account storedAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("15.00").compareTo(storedAccount.getBalance()));
    }
}
