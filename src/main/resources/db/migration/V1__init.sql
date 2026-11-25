-- V1: initial schema for the retail transaction pipeline

CREATE TABLE retail_transactions (
    transaction_id  VARCHAR(64)  NOT NULL,
    store_id        VARCHAR(64)  NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL,
    payment_method  VARCHAR(32)  NOT NULL,
    item_count      INTEGER      NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_retail_transactions PRIMARY KEY (transaction_id)
);

CREATE INDEX idx_retail_tx_store_occurred
    ON retail_transactions (store_id, occurred_at);

-- Idempotency ledger: one row per fully processed transaction id.
CREATE TABLE processed_events (
    transaction_id  VARCHAR(64)  NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_processed_events PRIMARY KEY (transaction_id)
);
