package com.arshadansari.retailpipeline.event;

import java.math.BigDecimal;
import java.time.Instant;

// What gets published to the retail-transactions topic.
public record TransactionEvent(
        String transactionId,
        String storeId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        int itemCount,
        Instant occurredAt
) {
}
