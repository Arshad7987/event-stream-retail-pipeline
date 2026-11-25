package com.arshadansari.retailpipeline;

import com.arshadansari.retailpipeline.event.TransactionEvent;
import com.arshadansari.retailpipeline.service.TransactionProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Full flow on the test profile: H2 instead of Postgres, in-memory cache
// instead of Redis, and no Kafka broker — the processing service is called
// directly, the way the consumer would call it. Keeps the test fast and
// deterministic while still going through the real persistence code.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionProcessingService processingService;

    // Replaces the real producer so no broker is needed; send() is a no-op mock.
    @MockBean
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Test
    void transactionFlowsFromApiToDailySummaryExactlyOnce() throws Exception {
        // 1. Submit through the real REST API (producer is mocked).
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "ITX-1",
                                  "storeId": "STORE-IT",
                                  "amount": 100.00,
                                  "currency": "INR",
                                  "paymentMethod": "UPI",
                                  "itemCount": 4,
                                  "occurredAt": "2026-11-25T07:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted());

        // 2. Simulate the consumer receiving the event — twice, as Kafka's
        //    at-least-once delivery can do after a rebalance.
        TransactionEvent event = new TransactionEvent(
                "ITX-1", "STORE-IT", new BigDecimal("100.00"), "INR", "UPI", 4,
                Instant.parse("2026-11-25T07:00:00Z"));
        assertThat(processingService.process(event)).isTrue();
        assertThat(processingService.process(event)).isFalse(); // duplicate skipped

        // 3. The daily summary must count the transaction exactly once.
        mockMvc.perform(get("/api/v1/stores/STORE-IT/summary")
                        .param("date", "2026-11-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(1))
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.totalItems").value(4));
    }

    @Test
    void summaryIsZeroForStoreWithoutTransactions() throws Exception {
        mockMvc.perform(get("/api/v1/stores/STORE-EMPTY/summary")
                        .param("date", "2026-11-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }
}
