package com.arshadansari.retailpipeline.consumer;

import com.arshadansari.retailpipeline.event.TransactionEvent;
import com.arshadansari.retailpipeline.service.TransactionProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

// Manual ack: the offset is only committed after processing succeeds, so a
// failed record gets redelivered (and eventually dead-lettered, see KafkaConfig).
@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final TransactionProcessingService processingService;

    public TransactionConsumer(TransactionProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(topics = "${app.kafka.transactions-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(TransactionEvent event, Acknowledgment acknowledgment) {
        log.debug("Received transaction event transactionId={} storeId={}",
                event.transactionId(), event.storeId());
        processingService.process(event);
        acknowledgment.acknowledge();
    }
}
