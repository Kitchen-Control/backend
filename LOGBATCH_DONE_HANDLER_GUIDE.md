# Hướng dẫn Implement: Xử lý Nhập kho khi LogBatch Hoàn tất

Tài liệu này hướng dẫn chi tiết cách implement nghiệp vụ tự động nhập kho khi một `LogBatch` được chuyển trạng thái sang `DONE`.

## 1. Mục tiêu

Khi một `LogBatch` (lô sản xuất hoặc lô mua hàng) được xác nhận hoàn tất (`status` = `DONE`), hệ thống phải tự động thực hiện hai việc:
1.  **Tạo `Inventory`:** Tạo một bản ghi mới trong bảng `inventories` để thể hiện rằng lô hàng này hiện đã có trong kho với số lượng tương ứng.
2.  **Tạo `InventoryTransaction`:** Ghi lại một dòng lịch sử giao dịch (`type` = `IMPORT`) để theo dõi việc nhập kho này.

## 2. Kiến trúc đề xuất: Dùng Spring Events & Handlers

Để giữ cho code "sạch" và dễ bảo trì, chúng ta sẽ sử dụng cơ chế Event-Listener của Spring.

*   **`LogBatchService`**: Sau khi cập nhật trạng thái của `LogBatch` thành `DONE`, nó sẽ **phát ra một sự kiện** (ví dụ: `LogBatchStatusChangedEvent`). Nó không cần biết ai sẽ xử lý sự kiện này.
*   **`DoneLogBatchHandler`**: Một class riêng biệt, sẽ **lắng nghe** sự kiện trên. Khi nhận được sự kiện, nó sẽ kiểm tra xem trạng thái mới có phải là `DONE` không, và nếu đúng, nó sẽ thực thi toàn bộ logic nhập kho.

### Lợi ích của kiến trúc này:
*   **Decoupling (Tách biệt):** `LogBatchService` không bị "dính" với `InventoryService` hay `InventoryTransactionService`.
*   **Single Responsibility:** Mỗi class làm một việc duy nhất. `LogBatchService` lo việc của `LogBatch`, `DoneLogBatchHandler` lo việc nhập kho.
*   **Dễ mở rộng:** Nếu sau này bạn muốn gửi email khi nhập kho xong, bạn chỉ cần tạo thêm một `EmailHandler` khác lắng nghe cùng sự kiện đó.

---

## 3. Các bước Implement

### Bước 1: Tạo Event Class (Nếu chưa có)

Tạo một class để chứa thông tin về sự kiện thay đổi trạng thái.

```java
// File: src/main/java/org/luun/kitchencontrolbev1/event/LogBatchStatusChangedEvent.java
package org.luun.kitchencontrolbev1.event;

import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.springframework.context.ApplicationEvent;

public class LogBatchStatusChangedEvent extends ApplicationEvent {
    private final LogBatch logBatch;

    public LogBatchStatusChangedEvent(Object source, LogBatch logBatch) {
        super(source);
        this.logBatch = logBatch;
    }

    public LogBatch getLogBatch() {
        return logBatch;
    }
}
```

### Bước 2: Tạo Handler để xử lý logic Nhập kho

Đây là nơi chứa logic nghiệp vụ chính.

```java
// File: src/main/java/org/luun/kitchencontrolbev1/handler/logbatch/DoneLogBatchHandler.java
package org.luun.kitchencontrolbev1.handler.logbatch;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.entity.Inventory;
import org.luun.kitchencontrolbev1.entity.InventoryTransaction;
import org.luun.kitchencontrolbev1.entity.LogBatch;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.event.LogBatchStatusChangedEvent;
import org.luun.kitchencontrolbev1.repository.InventoryRepository;
import org.luun.kitchencontrolbev1.repository.InventoryTransactionRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DoneLogBatchHandler {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @EventListener
    @Transactional
    public void handleLogBatchDone(LogBatchStatusChangedEvent event) {
        LogBatch logBatch = event.getLogBatch();

        // Chỉ xử lý khi trạng thái là DONE
        if (logBatch.getStatus() != LogBatchStatus.DONE) {
            return;
        }

        // 1. Kiểm tra Idempotency: Đảm bảo logic không chạy 2 lần cho cùng 1 lô
        if (inventoryRepository.existsByBatch(logBatch)) {
            System.out.println("Cảnh báo: Lô " + logBatch.getBatchId() + " đã được nhập kho trước đó. Bỏ qua.");
            return;
        }

        // 2. Tạo bản ghi Inventory mới
        Inventory inventory = new Inventory();
        inventory.setProduct(logBatch.getProduct());
        inventory.setBatch(logBatch);
        inventory.setQuantity(logBatch.getQuantity());
        inventory.setExpiryDate(logBatch.getExpiryDate());
        inventory.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inventory);

        // 3. Tạo bản ghi InventoryTransaction (Lịch sử nhập kho)
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(logBatch.getProduct());
        transaction.setBatch(logBatch);
        transaction.setQuantity(logBatch.getQuantity());
        transaction.setType(InventoryTransactionType.IMPORT); // Loại giao dịch là NHẬP KHO
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setNote("Nhập kho từ lô " + (logBatch.getType() == org.luun.kitchencontrolbev1.enums.LogBatchType.PRODUCTION ? "sản xuất" : "mua hàng") + " ID: " + logBatch.getBatchId());
        inventoryTransactionRepository.save(transaction);
        
        System.out.println("Đã nhập kho thành công cho lô: " + logBatch.getBatchId());
    }
}
```

### Bước 3: Refactor `LogBatchServiceImpl` để phát sự kiện

Sửa lại hàm `updateLogBatchStatus` (hoặc hàm `complete` trong State Machine) để nó chỉ phát ra sự kiện.

```java
// File: src/main/java/org/luun/kitchencontrolbev1/service/impl/LogBatchServiceImpl.java

import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {

    private final LogBatchRepository logBatchRepository;
    private final ApplicationEventPublisher eventPublisher; // Inject cái này
    // ... các repository khác

    @Override
    @Transactional
    public LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus newStatus) {
        LogBatch logBatch = logBatchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("LogBatch not found"));

        // Dùng State Machine để chuyển trạng thái
        // getState(logBatch.getStatus()).update(logBatch, newStatus);
        logBatch.setStatus(newStatus); // Giả sử đã chuyển trạng thái thành công

        LogBatch savedBatch = logBatchRepository.save(logBatch);

        // Phát ra sự kiện sau khi đã lưu thành công
        eventPublisher.publishEvent(new LogBatchStatusChangedEvent(this, savedBatch));

        return mapToResponse(savedBatch);
    }
    
    // ... các hàm khác
}
```

## 4. Tổng kết

Với cách tổ chức này:
*   **`LogBatchServiceImpl`** trở nên rất gọn nhẹ, chỉ làm đúng nhiệm vụ quản lý `LogBatch`.
*   **`DoneLogBatchHandler`** chứa toàn bộ nghiệp vụ phức tạp liên quan đến việc nhập kho.
*   Hệ thống của bạn giờ đây rất linh hoạt. Nếu có thêm nghiệp vụ mới khi `LogBatch` chuyển sang `DAMAGED`, bạn chỉ cần tạo một `DamagedLogBatchHandler` mới mà không cần đụng đến code cũ.
