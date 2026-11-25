package com.arshadansari.retailpipeline.service;

import com.arshadansari.retailpipeline.api.TransactionRequest;
import com.arshadansari.retailpipeline.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionIngestionService.class);

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final String topic;

    public TransactionIngestionService(KafkaTemplate<String, TransactionEvent> kafkaTemplate,
                                       @Value("${app.kafka.transactions-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void ingest(TransactionRequest request) {
        TransactionEvent event = new TransactionEvent(
                request.transactionId(),
                request.storeId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.itemCount(),
                request.occurredAtOrNow());

        // storeId as the record key: all events for one store land on the same
        // partition, so they are consumed in order.
        kafkaTemplate.send(topic, event.storeId(), event);
        log.info("Accepted transaction {} for store {}", event.transactionId(), event.storeId());
    }
}
