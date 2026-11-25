package com.arshadansari.retailpipeline.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "retail_transactions")
public class RetailTransaction {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 64)
    private String transactionId;

    @Column(name = "store_id", nullable = false, length = 64)
    private String storeId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod;

    @Column(name = "item_count", nullable = false)
    private int itemCount;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected RetailTransaction() {
        // JPA
    }

    public RetailTransaction(String transactionId, String storeId, BigDecimal amount, String currency,
                             String paymentMethod, int itemCount, Instant occurredAt, Instant processedAt) {
        this.transactionId = transactionId;
        this.storeId = storeId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.itemCount = itemCount;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStoreId() {
        return storeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public int getItemCount() {
        return itemCount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
