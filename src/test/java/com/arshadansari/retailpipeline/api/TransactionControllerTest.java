package com.arshadansari.retailpipeline.api;

import com.arshadansari.retailpipeline.service.TransactionIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionIngestionService ingestionService;

    @Test
    void acceptsValidTransaction() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "TX-300",
                                  "storeId": "STORE-1",
                                  "amount": 250.00,
                                  "currency": "INR",
                                  "paymentMethod": "UPI",
                                  "itemCount": 2,
                                  "occurredAt": "2026-11-25T09:30:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value("TX-300"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void rejectsNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "TX-301",
                                  "storeId": "STORE-1",
                                  "amount": -5.00,
                                  "currency": "INR",
                                  "paymentMethod": "UPI",
                                  "itemCount": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void rejectsMissingStoreId() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "TX-302",
                                  "amount": 10.00,
                                  "currency": "INR",
                                  "paymentMethod": "CARD",
                                  "itemCount": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.storeId").exists());
    }
}
