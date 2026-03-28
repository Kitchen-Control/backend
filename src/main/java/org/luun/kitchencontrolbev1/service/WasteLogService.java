package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.WasteLogRequest;
import org.luun.kitchencontrolbev1.dto.response.WasteLogResponse;

import java.time.LocalDate;
import java.util.List;

public interface WasteLogService {
    List<WasteLogResponse> getAllWasteLogs();
    WasteLogResponse createWasteLog(WasteLogRequest request);
    List<WasteLogResponse> getWasteLogsByDateRange(LocalDate startDate, LocalDate endDate);
}
