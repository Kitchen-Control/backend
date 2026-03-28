package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WasteLogRepository extends JpaRepository<WasteLog, Integer> {
    List<WasteLog> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
