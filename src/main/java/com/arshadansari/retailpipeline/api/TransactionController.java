package com.arshadansari.retailpipeline.api;

import com.arshadansari.retailpipeline.service.TransactionIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionIngestionService ingestionService;

    public TransactionController(TransactionIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    // 202 on purpose: the transaction is queued on Kafka at this point, not
    // yet persisted, so the daily summary may not include it right away.
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAcceptedResponse submit(@Valid @RequestBody TransactionRequest request) {
        ingestionService.ingest(request);
        return new TransactionAcceptedResponse(request.transactionId(), "ACCEPTED");
    }
}
