package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.domain.RetailTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreSummaryServiceTest {

    @Mock
    private RetailTransactionRepository repository;

    @Test
    void aggregatesSummaryForTheUtcDayWindow() {
        StoreSummaryService service = new StoreSummaryService(repository);
        LocalDate date = LocalDate.of(2026, 11, 25);
        Instant from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        when(repository.countForStoreBetween("STORE-1", from, to)).thenReturn(42L);
        when(repository.sumAmountForStoreBetween("STORE-1", from, to))
                .thenReturn(new BigDecimal("9876.54"));
        when(repository.sumItemsForStoreBetween("STORE-1", from, to)).thenReturn(130L);

        var summary = service.dailySummary("STORE-1", date);

        assertThat(summary.storeId()).isEqualTo("STORE-1");
        assertThat(summary.date()).isEqualTo(date);
        assertThat(summary.transactionCount()).isEqualTo(42L);
        assertThat(summary.totalAmount()).isEqualByComparingTo("9876.54");
        assertThat(summary.totalItems()).isEqualTo(130L);
    }
}
