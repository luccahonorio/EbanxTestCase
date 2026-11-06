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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.clear();
    }


    @Test
    void shouldReturnAccountBalance() throws Exception {
        accountRepository.save(new Account("123", new BigDecimal("150")));

        mockMvc.perform(get("/balance")
                        .param("account_id", "123"))
                .andExpect(status().isOk())
                .andExpect(content().string("150"));
    }

    @Test
    void shouldReturnNotFoundWhenAccountDoesNotExist() throws  Exception{
        mockMvc.perform(get("/balance")
                        .param("account_id", "999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0"));
        assertTrue(accountRepository.findById("999").isEmpty());
    }
}
