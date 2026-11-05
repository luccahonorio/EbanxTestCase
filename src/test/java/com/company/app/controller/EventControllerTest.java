
package com.company.app.controller;

import com.company.app.domain.Account;
import com.company.app.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.clear();
    }

    @Test
    void shouldDepositAndReturnCreatedResponse() throws Exception {
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "deposit",
                                    "destination": "100",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destination.id").value("100"))
                .andExpect(jsonPath("$.destination.balance").value(100))
                .andDo(result -> assertEquals(
                        0,
                        accountRepository.findById("100")
                                .orElseThrow()
                                .getBalance()
                                .compareTo(new BigDecimal("100"))
                ));
    }

    @Test
    void shouldWithdrawAndReturnCreatedResponse() throws Exception {
        accountRepository.save(new Account("200", new BigDecimal("150")));

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "withdraw",
                                    "origin": "200",
                                    "amount": 50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.id").value("200"))
                .andExpect(jsonPath("$.origin.balance").value(100))
                .andDo(result -> assertEquals(
                        0,
                        accountRepository.findById("200")
                                .orElseThrow()
                                .getBalance()
                                .compareTo(new BigDecimal("100"))
                ));
    }

    @Test
    void shouldReturnErrorWhenWithdrawingMoreThanBalance() throws Exception {
        accountRepository.save(new Account("300", new BigDecimal("50")));

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "withdraw",
                                    "origin": "300",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient funds"))
                .andDo(result -> assertEquals(
                        0,
                        accountRepository.findById("300")
                                .orElseThrow()
                                .getBalance()
                                .compareTo(new BigDecimal("50"))
                ));
    }

    @Test
    void shouldReturnNotFoundWhenWithdrawingFromNonexistentAccount() throws Exception {
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "withdraw",
                                    "origin": "999",
                                    "amount": 50
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"))
                .andDo(result -> assertTrue(
                        accountRepository.findById("999").isEmpty()
                ));
    }

    @Test
    void shouldTransferBetweenExistingAccountsAndReturnCreatedResponse() throws Exception {
        accountRepository.save(new Account("400", new BigDecimal("200")));
        accountRepository.save(new Account("500", new BigDecimal("100")));

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "transfer",
                                    "origin": "400",
                                    "destination": "500",
                                    "amount": 50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.id").value("400"))
                .andExpect(jsonPath("$.origin.balance").value(150))
                .andExpect(jsonPath("$.destination.id").value("500"))
                .andExpect(jsonPath("$.destination.balance").value(150))
                .andDo(result -> {
                    Account originAccount = accountRepository.findById("400").orElseThrow();
                    Account destinationAccount = accountRepository.findById("500").orElseThrow();

                    assertEquals(0, originAccount.getBalance().compareTo(new BigDecimal("150")));
                    assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("150")));
                });
    }

    @Test
    void shouldTransferToNonexistentDestinationAndCreateAccount() throws Exception {
        accountRepository.save(new Account("600", new BigDecimal("300")));

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "transfer",
                                    "origin": "600",
                                    "destination": "700",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.id").value("600"))
                .andExpect(jsonPath("$.origin.balance").value(200))
                .andExpect(jsonPath("$.destination.id").value("700"))
                .andExpect(jsonPath("$.destination.balance").value(100))
                .andDo(result -> {
                    Account originAccount = accountRepository.findById("600").orElseThrow();
                    Account destinationAccount = accountRepository.findById("700").orElseThrow();

                    assertEquals(0, originAccount.getBalance().compareTo(new BigDecimal("200")));
                    assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("100")));
                });
    }

    @Test
    void shouldReturnErrorWhenTransferringMoreThanBalance() throws Exception{
        accountRepository.save(new Account("800", new BigDecimal("50")));
        accountRepository.save(new Account("900", new BigDecimal("100")));

        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "transfer",
                                    "origin": "800",
                                    "destination": "900",
                                    "amount": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient funds"))
                .andDo(result -> {
                    Account originAccount = accountRepository.findById("800").orElseThrow();
                    Account destinationAccount = accountRepository.findById("900").orElseThrow();

                    assertEquals(0, originAccount.getBalance().compareTo(new BigDecimal("50")));
                    assertEquals(0, destinationAccount.getBalance().compareTo(new BigDecimal("100")));
                });
    }

    @Test
    void shouldReturnNotFoundWhenTransferringFromNonexistentAccount() throws Exception{
        mockMvc.perform(post("/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "type": "transfer",
                                    "origin": "999",
                                    "destination": "1000",
                                    "amount": 50
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"))
                .andDo(result -> assertTrue(
                        accountRepository.findById("999").isEmpty() &&
                        accountRepository.findById("1000").isEmpty()
                ));
    }
}
