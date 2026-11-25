package com.arshadansari.retailpipeline.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// Topics, plus the consumer error handler: retry a failing record 3 times,
// then send it to the dead-letter topic instead of blocking the partition.
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic transactionsTopic(@Value("${app.kafka.transactions-topic}") String topic) {
        return TopicBuilder.name(topic)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionsDeadLetterTopic(@Value("${app.kafka.transactions-topic}") String topic) {
        return TopicBuilder.name(topic + ".DLT")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3L));
    }
}
