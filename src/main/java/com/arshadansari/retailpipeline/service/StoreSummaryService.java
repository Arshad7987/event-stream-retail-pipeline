package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.api.StoreDailySummaryResponse;
import com.arshadansari.retailpipeline.domain.RetailTransactionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class StoreSummaryService {

    private final RetailTransactionRepository transactionRepository;

    public StoreSummaryService(RetailTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    // Cached in Redis — a day's numbers are read a lot more often than they
    // change. Key format storeId:date matches the eviction in
    // TransactionProcessingService.
    @Cacheable(value = TransactionProcessingService.SUMMARY_CACHE,
            key = "#storeId + ':' + #date")
    @Transactional(readOnly = true)
    public StoreDailySummaryResponse dailySummary(String storeId, LocalDate date) {
        var from = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        var to = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        long count = transactionRepository.countForStoreBetween(storeId, from, to);
        BigDecimal total = transactionRepository.sumAmountForStoreBetween(storeId, from, to);
        long items = transactionRepository.sumItemsForStoreBetween(storeId, from, to);

        return new StoreDailySummaryResponse(storeId, date, count, total, items);
    }
}
