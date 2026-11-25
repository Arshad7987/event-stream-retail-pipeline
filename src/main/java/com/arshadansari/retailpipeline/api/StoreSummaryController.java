package com.arshadansari.retailpipeline.api;

import com.arshadansari.retailpipeline.service.StoreSummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/stores")
public class StoreSummaryController {

    private final StoreSummaryService summaryService;

    public StoreSummaryController(StoreSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/{storeId}/summary")
    public StoreDailySummaryResponse dailySummary(
            @PathVariable String storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return summaryService.dailySummary(storeId, date);
    }
}
