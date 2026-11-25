package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.domain.ProcessedEvent;
import com.arshadansari.retailpipeline.domain.ProcessedEventRepository;
import com.arshadansari.retailpipeline.domain.RetailTransaction;
import com.arshadansari.retailpipeline.domain.RetailTransactionRepository;
import com.arshadansari.retailpipeline.event.TransactionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionProcessingServiceTest {

    @Mock
    private RetailTransactionRepository transactionRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private TransactionProcessingService service;

    private final TransactionEvent event = new TransactionEvent(
            "TX-200", "STORE-9", new BigDecimal("1250.50"), "INR", "CARD", 5,
            Instant.parse("2026-11-25T08:00:00Z"));

    @BeforeEach
    void setUp() {
        CacheManager cacheManager =
                new ConcurrentMapCacheManager(TransactionProcessingService.SUMMARY_CACHE);
        service = new TransactionProcessingService(
                transactionRepository, processedEventRepository, cacheManager);
    }

    @Test
    void processesNewEventAndWritesLedgerRow() {
        when(processedEventRepository.existsById("TX-200")).thenReturn(false);

        boolean processed = service.process(event);

        assertThat(processed).isTrue();
        ArgumentCaptor<RetailTransaction> txCaptor = ArgumentCaptor.forClass(RetailTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getTransactionId()).isEqualTo("TX-200");
        assertThat(txCaptor.getValue().getStoreId()).isEqualTo("STORE-9");
        assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("1250.50");

        ArgumentCaptor<ProcessedEvent> ledgerCaptor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(ledgerCaptor.capture());
        assertThat(ledgerCaptor.getValue().getTransactionId()).isEqualTo("TX-200");
    }

    @Test
    void skipsDuplicateEventWithoutSavingAnything() {
        when(processedEventRepository.existsById("TX-200")).thenReturn(true);

        boolean processed = service.process(event);

        assertThat(processed).isFalse();
        verify(transactionRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }
}
