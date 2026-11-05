package com.company.app.controller;

import com.company.app.dto.request.EventRequest;
import com.company.app.dto.response.EventResponse;
import com.company.app.dto.response.AccountResponse;
import com.company.app.domain.Account;
import com.company.app.domain.EventType;
import com.company.app.domain.TransferResult;
import com.company.app.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
public class EventController {
    private final AccountService accountService;

    public EventController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/event")
    public ResponseEntity<EventResponse> handleEvent(@RequestBody EventRequest request) {
        if (request.type() == null || request.type().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event type is required");
        }

        EventType type;
        try {
            type = EventType.valueOf(request.type().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported event type", exception);
        }

        EventResponse response = switch (type) {
            case DEPOSIT -> {
                Account destination = accountService.deposit(request.destination(), request.amount());
                yield new EventResponse(null, toResponse(destination));
            }
            case WITHDRAW -> {
                Account origin = accountService.withdraw(request.origin(), request.amount());
                yield new EventResponse(toResponse(origin), null);
            }
            case TRANSFER -> {
                TransferResult transfer = accountService.transfer(
                        request.origin(), request.destination(), request.amount());
                yield new EventResponse(
                        toResponse(transfer.origin()),
                        toResponse(transfer.destination()));
            }
        };

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getBalance());
    }
}
