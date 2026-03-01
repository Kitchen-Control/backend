# Feature Specification: Get All Waiting Orders

Tài liệu này hướng dẫn chi tiết các bước để implement chức năng lấy danh sách các đơn hàng đang ở trạng thái **WAITTING**.

## 1. API Endpoint Specification

*   **URL**: `/api/orders/waiting`
*   **Method**: `GET`
*   **Description**: Lấy danh sách tất cả các đơn hàng chưa được xử lý (Status = `WAITTING`).
*   **Auth**: Yêu cầu quyền Admin hoặc Staff (tùy logic dự án).

### Response Example (200 OK)
```json
[
  {
    "orderId": 10,
    "storeId": 1,
    "storeName": "Cửa hàng A",
    "orderDate": "2023-10-27T10:00:00",
    "status": "WAITTING",
    "comment": "Giao nhanh",
    "orderDetails": [
      {
        "productId": 101,
        "productName": "Gà rán",
        "quantity": 2.0
      }
    ]
  }
]
```

---

## 2. Implementation Steps

### Step 1: Repository Layer (`OrderRepository.java`)

Thêm method để tìm kiếm theo trạng thái `status`. Spring Data JPA sẽ tự động generate query dựa trên tên method.

```java
// File: org/luun/kitchencontrolbev1/repository/OrderRepository.java

import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // ... các method cũ

    // Thêm dòng này:
    List<Order> findByStatus(OrderStatus status);
}
```

### Step 2: Service Layer

#### Interface (`OrderService.java`)

Khai báo method mới trong interface.

```java
// File: org/luun/kitchencontrolbev1/service/OrderService.java

public interface OrderService {
    // ... các method cũ
    
    List<OrderResponse> getWaitingOrders();
}
```

#### Implementation (`OrderServiceImpl.java`)

Implement logic để gọi repository và map dữ liệu sang DTO. Lưu ý sử dụng đúng Enum `OrderStatus.WAITTING`.

```java
// File: org/luun/kitchencontrolbev1/service/impl/OrderServiceImpl.java

@Override
public List<OrderResponse> getWaitingOrders() {
    // 1. Lấy danh sách entity từ DB theo status WAITTING
    List<Order> orders = orderRepository.findByStatus(OrderStatus.WAITTING);
    
    // 2. Map từ Entity sang Response DTO (sử dụng hàm mapToResponse có sẵn)
    return orders.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
}
```

### Step 3: Controller Layer (`OrderController.java`)

Tạo endpoint để FE gọi vào.

```java
// File: org/luun/kitchencontrolbev1/controller/OrderController.java

@GetMapping("/waiting")
public ResponseEntity<List<OrderResponse>> getWaitingOrders() {
    List<OrderResponse> orders = orderService.getWaitingOrders();
    return ResponseEntity.ok(orders);
}
```

---

## 3. Notes
*   Đảm bảo rằng trong Database, cột `status` đang lưu giá trị chuỗi là `"WAITTING"` (khớp với Enum trong code Java).
*   Hàm `mapToResponse` đã tồn tại trong `OrderServiceImpl`, bạn chỉ cần tái sử dụng nó.
