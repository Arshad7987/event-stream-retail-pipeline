package com.arshadansari.retailpipeline.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

// transactionId comes from the caller (POS terminal) and doubles as the
// idempotency key for the whole pipeline.
public record TransactionRequest(
        @NotBlank(message = "transactionId is required")
        String transactionId,

        @NotBlank(message = "storeId is required")
        String storeId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code")
        String currency,

        @NotBlank(message = "paymentMethod is required")
        String paymentMethod,

        @PositiveOrZero(message = "itemCount cannot be negative")
        int itemCount,

        Instant occurredAt
) {
    public Instant occurredAtOrNow() {
        return occurredAt != null ? occurredAt : Instant.now();
    }
}
