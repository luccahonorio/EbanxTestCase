package com.company.app.dto.response;

import java.math.BigDecimal;

public record AccountResponse(
        String id,
        BigDecimal balance
) {
}
