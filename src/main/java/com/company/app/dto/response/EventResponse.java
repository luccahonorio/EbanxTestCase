package com.company.app.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(
        AccountResponse origin,
        AccountResponse destination
) {
}
