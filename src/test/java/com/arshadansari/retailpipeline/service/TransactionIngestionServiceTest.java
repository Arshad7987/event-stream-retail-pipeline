package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.api.TransactionRequest;
import com.arshadansari.retailpipeline.event.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionIngestionServiceTest {

    @Mock
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Test
    void publishesEventKeyedByStoreId() {
        TransactionIngestionService service =
                new TransactionIngestionService(kafkaTemplate, "retail-transactions");

        Instant occurredAt = Instant.parse("2026-11-25T10:15:00Z");
        service.ingest(new TransactionRequest(
                "TX-100", "STORE-1", new BigDecimal("499.99"), "INR", "UPI", 3, occurredAt));

        ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(kafkaTemplate).send(eq("retail-transactions"), eq("STORE-1"), captor.capture());
        TransactionEvent event = captor.getValue();
        assertThat(event.transactionId()).isEqualTo("TX-100");
        assertThat(event.amount()).isEqualByComparingTo("499.99");
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void defaultsOccurredAtToNowWhenMissing() {
        TransactionIngestionService service =
                new TransactionIngestionService(kafkaTemplate, "retail-transactions");

        service.ingest(new TransactionRequest(
                "TX-101", "STORE-2", new BigDecimal("10.00"), "USD", "CARD", 1, null));

        ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(kafkaTemplate).send(eq("retail-transactions"), eq("STORE-2"), captor.capture());
        assertThat(captor.getValue().occurredAt()).isNotNull();
    }
}
