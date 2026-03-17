package org.luun.kitchencontrolbev1.repository;

import org.luun.kitchencontrolbev1.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WasteLogRepository extends JpaRepository<WasteLog, Integer> {
}
