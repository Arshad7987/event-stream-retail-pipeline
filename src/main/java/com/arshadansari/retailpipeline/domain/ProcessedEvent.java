package com.arshadansari.retailpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

// One row per processed transaction id. This is the dedupe ledger the
// consumer checks before processing an event.
@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 64)
    private String transactionId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
        // JPA
    }

    public ProcessedEvent(String transactionId, Instant processedAt) {
        this.transactionId = transactionId;
        this.processedAt = processedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
