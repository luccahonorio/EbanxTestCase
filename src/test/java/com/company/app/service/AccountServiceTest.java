package com.company.app.service;

import com.company.app.domain.Account;
import com.company.app.domain.TransferResult;
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

        // Assert
        assertEquals("Account not found", exception.getMessage());
        assertTrue(repository.findById("999").isEmpty());
    }

    @Test
    void shouldTransferBetweenExistingAccounts() {
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("100.00"));
        service.deposit("456", new BigDecimal("50.00"));

        // Act
        TransferResult result = service.transfer(
                "123", "456", new BigDecimal("30.00")
        );

        // Assert
        Account fromAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("70.00").compareTo(fromAccount.getBalance()));

        Account toAccount = repository.findById("456").orElseThrow();
        assertEquals(0, new BigDecimal("80.00").compareTo(toAccount.getBalance()));

        assertEquals("123", result.origin().getId());
        assertEquals(0, new BigDecimal("70.00").compareTo(result.origin().getBalance()));

        assertEquals("456", result.destination().getId());
        assertEquals(0, new BigDecimal("80.00").compareTo(result.destination().getBalance()));
    }

    @Test
    void shouldCreateDestinationAccountWhenTransferringToNonExistingAccount(){
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("100.00"));

        // Act
        service.transfer("123", "999", new BigDecimal("30.00"));

        // Assert
        Account fromAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("70.00").compareTo(fromAccount.getBalance()));
        Account toAccount = repository.findById("999").orElseThrow();
        assertEquals(0, new BigDecimal("30.00").compareTo(toAccount.getBalance()));
    }

    @Test
    void shouldThrowExceptionWhenTransferringMoreThanBalance() {
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("123", new BigDecimal("100.00"));
        service.deposit("456", new BigDecimal("50.00"));

        // Act
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.transfer("123", "456", new BigDecimal("150.00"))
        );

        // Assert
        assertEquals("Insufficient funds", exception.getMessage());

        // Validate that balances remain unchanged
        Account fromAccount = repository.findById("123").orElseThrow();
        assertEquals(0, new BigDecimal("100.00").compareTo(fromAccount.getBalance()));
        Account toAccount = repository.findById("456").orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(toAccount.getBalance()));
    }

    @Test
    void shouldThrowExceptionWhenTransferringFromNonExistingAccount(){
        // Arrange
        var repository = new InMemoryAccountRepository();
        var service = new AccountService(repository);
        service.deposit("456", new BigDecimal("50.00"));

        // Act
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> service.transfer("999", "456", new BigDecimal("30.00"))
        );

        // Assert
        assertEquals("Account not found", exception.getMessage());
        assertTrue(repository.findById("999").isEmpty());

        // validate that the destination account balance remains unchanged
        Account destinationAccount = repository.findById("456").orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(destinationAccount.getBalance()));
    }
}
