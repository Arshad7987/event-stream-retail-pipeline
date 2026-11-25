package com.arshadansari.retailpipeline.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record StoreDailySummaryResponse(
        String storeId,
        LocalDate date,
        long transactionCount,
        BigDecimal totalAmount,
        long totalItems
) implements Serializable {
}
