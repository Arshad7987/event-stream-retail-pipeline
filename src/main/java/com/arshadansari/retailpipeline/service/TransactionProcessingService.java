package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.domain.ProcessedEvent;
import com.arshadansari.retailpipeline.domain.ProcessedEventRepository;
import com.arshadansari.retailpipeline.domain.RetailTransaction;
import com.arshadansari.retailpipeline.domain.RetailTransactionRepository;
import com.arshadansari.retailpipeline.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

// Kafka delivers records at least once, so the same transaction can arrive
// twice. Before doing anything we check the processed_events table — if the
// transaction id is already there, the event is a redelivery and gets skipped.
// The transaction row and the ledger row are written in one DB transaction.
@Service
public class TransactionProcessingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessingService.class);
    public static final String SUMMARY_CACHE = "storeDailySummary";

    private final RetailTransactionRepository transactionRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final CacheManager cacheManager;

    public TransactionProcessingService(RetailTransactionRepository transactionRepository,
                                        ProcessedEventRepository processedEventRepository,
                                        CacheManager cacheManager) {
        this.transactionRepository = transactionRepository;
        this.processedEventRepository = processedEventRepository;
        this.cacheManager = cacheManager;
    }

    // returns false when the event turned out to be a duplicate
    @Transactional
    public boolean process(TransactionEvent event) {
        if (processedEventRepository.existsById(event.transactionId())) {
            log.info("Skipping duplicate transaction event transactionId={}", event.transactionId());
            return false;
        }

        Instant now = Instant.now();
        transactionRepository.save(new RetailTransaction(
                event.transactionId(),
                event.storeId(),
                event.amount(),
                event.currency(),
                event.paymentMethod(),
                event.itemCount(),
                event.occurredAt(),
                now));
        processedEventRepository.save(new ProcessedEvent(event.transactionId(), now));

        evictSummaryCache(event);
        log.info("Processed transaction transactionId={} storeId={}", event.transactionId(), event.storeId());
        return true;
    }

    private void evictSummaryCache(TransactionEvent event) {
        Cache cache = cacheManager.getCache(SUMMARY_CACHE);
        if (cache != null) {
            String key = event.storeId() + ":" + event.occurredAt().atZone(ZoneOffset.UTC).toLocalDate();
            cache.evict(key);
        }
    }
}
