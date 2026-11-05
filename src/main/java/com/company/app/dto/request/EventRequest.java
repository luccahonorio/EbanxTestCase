package com.company.app.dto.request;

import java.math.BigDecimal;

public record EventRequest(
    String type,
    String origin,
    String destination,
    BigDecimal amount
) {
}
