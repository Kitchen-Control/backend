# Chiến lược xử lý Logic khi Cập nhật Trạng thái LogBatch

Tài liệu này hướng dẫn cách tổ chức code khi việc cập nhật trạng thái (`status`) kéo theo nhiều logic nghiệp vụ phức tạp (Side Effects).

## 1. Vấn đề (The Problem)

Khi một `LogBatch` thay đổi trạng thái, nó không chỉ đơn thuần là update một trường trong database. Ví dụ:
*   **Status = DONE**: Lô hàng sản xuất xong -> Cần cộng số lượng vào kho (`Inventory`), tạo lịch sử giao dịch (`InventoryTransaction`).
*   **Status = WAITING_TO_CANCEL**: Lô hàng bị hỏng/hết hạn -> Cần khoá lô hàng, gửi thông báo cho quản lý, hoặc tạo phiếu hủy.

Nếu viết tất cả logic này vào trong một hàm `updateLogBatchStatus`, code sẽ trở nên rất dài, khó đọc và khó bảo trì (Spaghetti code).

## 2. Giải pháp: Chia nhỏ thành Private Methods

Bạn **NÊN** tách các logic xử lý riêng biệt ra thành các hàm `private`. Hàm chính `updateLogBatchStatus` chỉ đóng vai trò điều phối (Dispatcher).

### Cấu trúc đề xuất

```java
@Override
@Transactional
public LogBatchResponse updateLogBatchStatus(Integer batchId, LogBatchStatus newStatus) {
    // 1. Lấy LogBatch từ DB
    LogBatch logBatch = logBatchRepository.findById(batchId)
            .orElseThrow(() -> new RuntimeException("LogBatch not found"));

    // 2. Kiểm tra logic chuyển đổi trạng thái (Optional)
    // Ví dụ: Không thể chuyển từ CANCELLED về PROCESSING
    validateStatusTransition(logBatch.getStatus(), newStatus);

    // 3. Xử lý logic riêng cho từng trạng thái
    switch (newStatus) {
        case DONE:
            handleBatchDone(logBatch);
            break;
        case WAITING_TO_CANCLE:
            handleBatchWaitingToCancel(logBatch);
            break;
        case DAMAGED:
            handleBatchDamaged(logBatch);
            break;
        default:
            // Các trạng thái khác chỉ cần update status bình thường
            break;
    }

    // 4. Cập nhật status và lưu
    logBatch.setStatus(newStatus);
    return mapToResponse(logBatchRepository.save(logBatch));
}
```

## 3. Chi tiết các hàm Private (Helper Methods)

Dưới đây là ví dụ về những gì bạn cần làm trong các hàm phụ trợ.

### 3.1. Xử lý khi hoàn thành sản xuất (`DONE`)

Khi lô hàng xong, hàng phải được nhập vào kho.

```java
private void handleBatchDone(LogBatch logBatch) {
    // Logic: Cộng hàng vào kho (Inventory)
    
    // 1. Kiểm tra xem đã có Inventory cho lô này chưa (tránh cộng dồn 2 lần nếu lỡ bấm update 2 lần)
    if (inventoryRepository.existsByBatch(logBatch)) {
        return; // Hoặc throw exception tùy nghiệp vụ
    }

    // 2. Tạo Inventory mới
    Inventory inventory = new Inventory();
    inventory.setProduct(logBatch.getProduct());
    inventory.setBatch(logBatch);
    inventory.setQuantity(logBatch.getQuantity()); // Số lượng thực tế sản xuất
    inventory.setLastUpdated(LocalDateTime.now());
    inventoryRepository.save(inventory);

    // 3. Tạo Transaction log (Nhập kho từ sản xuất)
    InventoryTransaction trans = new InventoryTransaction();
    trans.setProduct(logBatch.getProduct());
    trans.setBatch(logBatch);
    trans.setQuantity(logBatch.getQuantity());
    trans.setType(InventoryTransactionType.IMPORT_FROM_PRODUCTION); // Cần thêm enum này
    trans.setCreatedAt(LocalDateTime.now());
    inventoryTransactionRepository.save(trans);
}
```

### 3.2. Xử lý khi chờ hủy (`WAITING_TO_CANCEL`)

Khi hàng hết hạn hoặc hỏng, cần đánh dấu để không bán/sử dụng nữa.

```java
private void handleBatchWaitingToCancel(LogBatch logBatch) {
    // Logic: Kiểm tra xem lô hàng này còn trong kho không
    Inventory inventory = inventoryRepository.findByBatch(logBatch).orElse(null);
    
    if (inventory != null && inventory.getQuantity() > 0) {
        // Có thể thực hiện logic "Khoá" kho, hoặc chỉ đơn giản là log lại cảnh báo
        System.out.println("Cảnh báo: Lô hàng " + logBatch.getBatchId() + " đang chờ hủy nhưng vẫn còn tồn kho!");
    }
    
    // Gửi thông báo (Notification) cho Manager nếu cần
}
```

## 4. Lợi ích
1.  **Dễ đọc:** Nhìn vào hàm chính là biết luồng đi như thế nào.
2.  **Dễ sửa:** Muốn sửa logic nhập kho, chỉ cần vào `handleBatchDone` sửa, không sợ ảnh hưởng đến logic hủy hàng.
3.  **Tái sử dụng:** Nếu có chỗ khác cần logic tương tự, có thể gọi lại các hàm private này.

## 5. Nâng cao (Design Pattern)
Nếu logic quá phức tạp (ví dụ mỗi trạng thái cần 50-100 dòng code), bạn nên cân nhắc sử dụng **State Pattern** hoặc **Spring Events** (ApplicationEventPublisher) để tách hẳn logic ra khỏi Service Class. Nhưng với quy mô hiện tại, dùng **Private Methods** là đủ và hiệu quả nhất.
