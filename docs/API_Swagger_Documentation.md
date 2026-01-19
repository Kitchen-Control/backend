# Hướng dẫn Tích hợp Swagger để Quản lý API

Tài liệu này sẽ hướng dẫn bạn từng bước cách tích hợp Swagger vào dự án Spring Boot để tự động tạo tài liệu và giao diện quản lý API một cách trực quan, dễ hiểu.

## Swagger và OpenAPI là gì?

*   **OpenAPI 3.0:** Là một tiêu chuẩn (specification) để mô tả các API REST. Nó định nghĩa cấu trúc của một API: các endpoint, tham số đầu vào, dữ liệu trả về, các mã lỗi,... dưới dạng một file JSON hoặc YAML.
*   **Swagger UI:** Là một công cụ hiển thị tài liệu API từ file JSON/YAML của OpenAPI một cách trực quan và đẹp mắt. Nó tạo ra một trang web cho phép bạn:
    *   Xem danh sách tất cả các API có trong dự án.
    *   Biết rõ từng API cần những tham số gì và sẽ trả về dữ liệu như thế nào.
    *   **Thử nghiệm (test) API ngay trên trình duyệt** mà không cần dùng đến Postman hay các công cụ khác.
*   **SpringDoc:** Là thư viện giúp kết nối Spring Boot với OpenAPI. Nó sẽ tự động "quét" code của bạn (cụ thể là các Controller) để sinh ra file tài liệu OpenAPI.

## Bước 1: Thêm Dependency vào `pom.xml`

Đây là bước quan trọng nhất. Bạn chỉ cần thêm **một** dependency vào file `pom.xml` của dự án. Thư viện này đã bao gồm cả bộ sinh tài liệu OpenAPI và giao diện Swagger UI.

Mở file `pom.xml` và thêm dependency sau vào trong thẻ `<dependencies>`:

```xml
<!-- Swagger (OpenAPI 3) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version> <!-- Bạn có thể kiểm tra phiên bản mới nhất trên Maven Central -->
</dependency>
```

Sau khi thêm, hãy để Maven tải dependency về (IDE của bạn thường sẽ tự động làm việc này).

## Bước 2: Chạy lại ứng dụng

Thật đáng kinh ngạc, bạn **không cần viết thêm bất kỳ dòng code Java nào** cho cấu hình cơ bản. Thư viện `springdoc` được thiết kế để tự động cấu hình (auto-configuration) với Spring Boot.

Việc bạn cần làm chỉ là **chạy lại ứng dụng Spring Boot** của mình.

## Bước 3: Truy cập Swagger UI

Sau khi ứng dụng đã khởi động thành công, hãy mở trình duyệt và truy cập vào địa chỉ sau:

**`http://localhost:8080/swagger-ui.html`**

*(Lưu ý: Thay `8080` bằng port của bạn nếu bạn có cấu hình port khác).*

Bạn sẽ thấy một giao diện web liệt kê tất cả các API có trong `OrderController` mà chúng ta đã tạo trước đó. Bạn có thể bấm vào từng API để xem chi tiết và dùng thử chúng.

Ngoài ra, bạn cũng có thể xem file JSON gốc của OpenAPI tại:
**`http://localhost:8080/v3/api-docs`**

## Bước 4: Làm cho tài liệu API chi tiết và hữu ích hơn

Giao diện mặc định đã hoạt động, nhưng chúng ta có thể làm cho nó tốt hơn rất nhiều bằng cách thêm các "chú thích" (annotations) ngay trong code Controller.

Hãy mở file `OrderController.java` và thêm các annotation sau từ `io.swagger.v3.oas.annotations`.

*   `@Tag`: Dùng để nhóm các API có liên quan.
*   `@Operation`: Dùng để mô tả chức năng của một API cụ thể.
*   `@ApiResponse`: Dùng để mô tả các kịch bản trả về của API (thành công, lỗi,...).

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/controllers/OrderController.java` (phiên bản cập nhật)

```java
package org.luun.hsf.asm01.kitchencontrolbe.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderResponseDTO;
import org.luun.hsf.asm01.kitchencontrolbe.services.IOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs để quản lý các đơn đặt hàng") // 1. Nhóm các API này lại
public class OrderController {

    private final IOrderService orderService;

    @Operation(summary = "Tạo một đơn hàng mới", description = "Tạo một đơn hàng từ ID cửa hàng và danh sách sản phẩm.") // 2. Mô tả API
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tạo đơn hàng thành công"), // 3. Mô tả các response
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        OrderResponseDTO newOrder = orderService.createOrder(request);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Lấy thông tin đơn hàng theo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tìm thấy đơn hàng"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng với ID cung cấp")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") int orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Lấy danh sách tất cả đơn hàng")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}
```

Bây giờ, hãy **chạy lại ứng dụng** và truy cập `http://localhost:8080/swagger-ui.html` một lần nữa. Bạn sẽ thấy tài liệu API đã trở nên chi tiết, chuyên nghiệp và dễ hiểu hơn rất nhiều.

## Bước 5 (Tùy chọn): Cấu hình thông tin chung cho API

Bạn có thể thêm các thông tin chung cho toàn bộ tài liệu API như tiêu đề, phiên bản, mô tả... bằng cách thêm các thuộc tính sau vào file `src/main/resources/application.properties`.

```properties
# SpringDoc OpenAPI General Configuration
springdoc.info.title=Kitchen Control BE API
springdoc.info.version=v1.0.0
springdoc.info.description=Tài liệu API cho hệ thống Backend của Kitchen Control.

# (Optional) Thay đổi đường dẫn của Swagger UI
# springdoc.swagger-ui.path=/my-api-docs.html
```

## Tổng kết

Chỉ với vài bước đơn giản, bạn đã tích hợp thành công một công cụ tài liệu API tự động cực kỳ mạnh mẽ. Điều này không chỉ giúp team backend dễ dàng quản lý API của mình mà còn giúp team frontend (hoặc bất kỳ ai sử dụng API) có thể hiểu và làm việc với API một cách hiệu quả mà không cần phải hỏi đi hỏi lại. Tài liệu sẽ luôn được cập nhật đồng bộ với code của bạn.
