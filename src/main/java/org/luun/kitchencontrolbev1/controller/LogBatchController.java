package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.LogBatchResponse;
import org.luun.kitchencontrolbev1.service.LogBatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log-batches")
@Tag(name = "Log Batches API", description = "API for managing log batches")
public class    LogBatchController {

    private final LogBatchService logBatchService;

    @GetMapping
    @Operation(summary = "Get all log batches")
    public List<LogBatchResponse> getAllLogBatches() {
        return logBatchService.getAllLogBatches();
    }

    @GetMapping("/{batchId}")
    @Operation(summary = "Get log batch by ID")
    public LogBatchResponse getLogBatchById(@PathVariable Integer batchId) {
        return logBatchService.getLogBatchById(batchId);
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "Get all log batches by Production Plan ID")
    public List<LogBatchResponse> getLogBatchesByPlanId(@PathVariable Integer planId) {
        return logBatchService.getLogBatchesByPlanId(planId);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get all log batches by Product ID")
    public List<LogBatchResponse> getLogBatchesByProductId(@PathVariable Integer productId) {
        return logBatchService.getLogBatchesByProductId(productId);
    }
}
