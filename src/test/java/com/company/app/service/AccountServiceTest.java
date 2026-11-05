package com.company.app.service;

import com.company.app.domain.Account;
import com.company.app.repository.InMemoryAccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void shouldReturnAccountWhenAccountExists(){
        //arange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("10.00"));

        //act
        Account account = service.getBalance("123").orElseThrow();

        //assert
        assertEquals("123", account.getId());
        assertEquals(0, new BigDecimal("10.00").compareTo(account.getBalance()));
    }

    @Test
    void shouldReturnEmptyWhenAccountDoesNotExist(){
        //arange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);

        //act
        var account = service.getBalance("999");

        //assert
        assertTrue(account.isEmpty());

        // Verify that the account was not created
        assertTrue(repository.findById("999").isEmpty());
    }

    @Test
    void shouldWithdrawWithSuccess() {
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("10.00"));

        // Act
        Account account = service.withdraw("123", new BigDecimal("5.00"));

        // Assert
        assertEquals("123", account.getId());
        assertEquals(0, new BigDecimal("5.00").compareTo(account.getBalance()));

        // Validate actual repository state
        Account storedAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("5.00").compareTo(storedAccount.getBalance()));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("10.00"));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.withdraw("123", new BigDecimal("15.00"))
        );

        // Assert
        assertEquals("Insufficient funds", exception.getMessage());

        //validate account balance remains unchanged
        Account account=repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("10.00").compareTo(account.getBalance()));
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingFromNonExistingAccount() {
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.withdraw("999", new BigDecimal("5.00"))
        );

        //assert
        assertEquals("Account not found", exception.getMessage());
        assertTrue(repository.findById("999").isEmpty());
    }
}
