package com.company.app.controller;

import com.company.app.domain.Account;
import com.company.app.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.clear();
    }

    @Test
    void shouldResetAllAccounts() throws Exception {
        accountRepository.save(new Account("100", new BigDecimal("150")));
        accountRepository.save(new Account("200", new BigDecimal("300")));

        mockMvc.perform(post("/reset"))
                .andExpect(status().isOk());

        assertTrue(accountRepository.findById("100").isEmpty());
        assertTrue(accountRepository.findById("200").isEmpty());
    }
}
