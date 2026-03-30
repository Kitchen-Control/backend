package org.luun.kitchencontrolbev1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.request.WasteLogRequest;
import org.luun.kitchencontrolbev1.dto.response.WasteLogResponse;
import org.luun.kitchencontrolbev1.service.WasteLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waste-log")
@Tag(name = "Waste Log API", description = "API for managing Waste Log")
public class WasteLogController {

    private final WasteLogService wasteLogService;

    @GetMapping
    public List<WasteLogResponse> getAll() {
        return wasteLogService.getAllWasteLogs();
    }

    @GetMapping("/getting-by-date-range")
    @Operation(summary = "Get waste logs by date range", description = "Retrieves waste logs that were created within the specified date range (e.g 2026-03-31).")
    public List<WasteLogResponse> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return wasteLogService.getWasteLogsByDateRange(startDate, endDate);
    }

    @PostMapping
    public WasteLogResponse create(WasteLogRequest request) {
        return wasteLogService.createWasteLog(request);
    }
}
