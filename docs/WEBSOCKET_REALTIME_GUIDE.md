# 🔌 Hướng Dẫn Tích Hợp WebSocket Thời Gian Thực - KitchenControlBEv1

> **Dự án:** KitchenControlBEv1  
> **Tech Stack:** Spring Boot 4.0.1, Java 17, PostgreSQL, Lombok, JWT Auth  
> **Base Package:** `org.luun.kitchencontrolbev1`

---

## 📋 Mục Lục

1. [Tổng Quan Dự Án Hiện Tại](#1-tổng-quan-dự-án-hiện-tại)
2. [WebSocket Là Gì & Tại Sao Cần Dùng](#2-websocket-là-gì--tại-sao-cần-dùng)
3. [Các Use Case WebSocket Phù Hợp Với Dự Án](#3-các-use-case-websocket-phù-hợp-với-dự-án)
4. [Bước 1: Thêm Dependency](#bước-1-thêm-dependency-vào-pomxml)
5. [Bước 2: Cấu Hình WebSocket + STOMP](#bước-2-tạo-file-cấu-hình-websocket)
6. [Bước 3: Cập Nhật Security Config](#bước-3-cập-nhật-securityconfig)
7. [Bước 4: Cập Nhật CORS Config](#bước-4-cập-nhật-cors-trong-webconfig)
8. [Bước 5: Tạo DTO Cho WebSocket Message](#bước-5-tạo-dto-cho-websocket-message)
9. [Bước 6: Tạo NotificationService](#bước-6-tạo-notificationservice)
10. [Bước 7: Tích Hợp Vào Service Hiện Có](#bước-7-tích-hợp-vào-các-service-hiện-có)
11. [Bước 8: Tạo WebSocket Controller (Tùy Chọn)](#bước-8-tạo-websocketcontroller-tùy-chọn)
12. [Bước 9: Kết Nối Từ Frontend](#bước-9-kết-nối-từ-frontend-reactjs)
13. [Bước 10: Kiểm Thử](#bước-10-kiểm-thử)
14. [Kiến Trúc Tổng Quan](#kiến-trúc-tổng-quan)
15. [Xử Lý Lỗi & Best Practices](#xử-lý-lỗi--best-practices)

---

## 1. Tổng Quan Dự Án Hiện Tại

### Kiến trúc Package
```
org.luun.kitchencontrolbev1
├── config/          → SecurityConfig, WebConfig
├── controller/      → 17 REST Controllers (Order, Inventory, Delivery, LogBatch, Receipt, ...)
├── dto/
│   ├── request/     → Các DTO nhận request (OrderRequest, LogBatchRequest, ...)
│   └── response/    → Các DTO trả response (OrderResponse, ApiResponse<T>, ...)
├── entity/          → 15 JPA Entities (Order, Product, Inventory, LogBatch, ...)
├── enums/           → OrderStatus, LogBatchStatus, ReceiptStatus, ...
├── exception/       → GlobalHandlerException
├── repository/      → 17 JPA Repositories
├── service/         → 17 Service interfaces
└── service/impl/    → 17 Service implementations
```

### Luồng nghiệp vụ chính
| # | Luồng | Trạng thái liên quan |
|---|-------|---------------------|
| 1 | **Đặt hàng** | Order: `WAITTING → PROCESSING → DISPATCHED → DELIVERING → DONE` |
| 2 | **Xuất kho** | Receipt: `DRAFT → COMPLETED`, kèm trừ kho tự động |
| 3 | **Giao hàng** | Delivery gán shipper, startDelivery, completeOrder |
| 4 | **Quản lý lô hàng** | LogBatch: `PROCESSING → DONE → WAITING_TO_CANCLE → DAMAGED` |
| 5 | **Kiểm tra hết hạn** | Scheduled Job chạy mỗi ngày lúc 00:01 |

---

## 2. WebSocket Là Gì & Tại Sao Cần Dùng

### HTTP truyền thống (Hiện tại của bạn)
```
Client ──GET /orders──► Server     (Client phải hỏi liên tục - Polling)
Client ◄──Response─────  Server
```

### WebSocket (Sau khi tích hợp)
```
Client ◄──────────────► Server     (Kết nối 2 chiều, Server tự đẩy data)
         Kết nối liên tục
         Server push khi có thay đổi
```

**Lợi ích:**
- ✅ Cập nhật trạng thái đơn hàng **ngay lập tức** cho tất cả client
- ✅ Thông báo hết hạn lô hàng **real-time** cho thủ kho
- ✅ Shipper nhận đơn mới **không cần refresh**
- ✅ Giảm tải server (không cần polling)

---

## 3. Các Use Case WebSocket Phù Hợp Với Dự Án

| Use Case | Topic STOMP | Khi nào gửi |
|----------|------------|-------------|
| Đơn hàng mới được tạo | `/topic/orders/new` | `OrderServiceImpl.createOrder()` |
| Trạng thái đơn hàng thay đổi | `/topic/orders/status` | `OrderServiceImpl.updateOrderStatus()` |
| Phiếu xuất kho được xác nhận | `/topic/receipts/confirmed` | `ReceiptServiceImpl.confirmReceipt()` |
| Shipper được gán chuyến | `/topic/deliveries/assigned` | `DeliveryServiceImpl.assignShipperToDelivery()` |
| Giao hàng bắt đầu | `/topic/deliveries/started` | `DeliveryServiceImpl.startDelivery()` |
| Lô hàng sắp hết hạn | `/topic/batches/expiring` | `LogBatchServiceImpl.updateExpiredBatches()` |
| Tồn kho thay đổi | `/topic/inventory/updated` | Khi trừ kho, nhập kho |
| Gửi riêng cho 1 store | `/topic/store/{storeId}` | Thông báo riêng cho từng cửa hàng |
| Gửi riêng cho 1 shipper | `/topic/shipper/{userId}` | Thông báo riêng cho shipper |

---

## Bước 1: Thêm Dependency Vào `pom.xml`

Mở file `pom.xml` và thêm dependency sau vào block `<dependencies>`:

```xml
<!-- WebSocket Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

**Vị trí thêm:** Ngay sau dependency `spring-boot-starter-webmvc` (dòng 40).

```xml
<!-- File: pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>

<!-- ✅ THÊM DÒNG NÀY -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

Sau đó chạy:
```bash
mvn clean install
# hoặc trong IntelliJ: nhấn nút "Reload Maven Projects" (biểu tượng 🔄)
```

---

## Bước 2: Tạo File Cấu Hình WebSocket

Tạo file mới: `src/main/java/org/luun/kitchencontrolbev1/config/WebSocketConfig.java`

```java
package org.luun.kitchencontrolbev1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker  // Kích hoạt xử lý message qua WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cấu hình Message Broker - nơi trung chuyển message giữa server và client.
     * 
     * Hãy tưởng tượng Message Broker giống như "Bưu điện":
     * - Client "đăng ký nhận thư" (subscribe) tại một địa chỉ (topic)
     * - Server "gửi thư" (send message) đến địa chỉ đó
     * - Broker sẽ chuyển thư đến tất cả client đã đăng ký
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix cho các DESTINATION mà client SUBSCRIBE (nhận message)
        // VD: client subscribe "/topic/orders/new" để nhận thông báo đơn hàng mới
        config.enableSimpleBroker("/topic");

        // Prefix cho các DESTINATION mà client GỬI message lên server
        // VD: client gửi message đến "/app/chat" → server xử lý
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Đăng ký endpoint mà client sẽ kết nối WebSocket đến.
     * 
     * - "/ws" là URL mà client dùng để thiết lập kết nối WebSocket ban đầu.
     * - SockJS là fallback khi trình duyệt không hỗ trợ WebSocket thuần.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                         // URL kết nối: ws://localhost:8080/ws
                .setAllowedOrigins(
                    "https://swp-web-six.vercel.app",       // Frontend trên Vercel
                    "http://localhost:3000"                  // Frontend local
                )
                .withSockJS();                              // Fallback cho trình duyệt cũ
    }
}
```

### Giải thích luồng hoạt động:

```
                    ┌──────────────────────────────────┐
                    │      Spring Boot Server           │
                    │                                    │
Client A ──ws://──►│  /ws (WebSocket Endpoint)          │
                    │       │                            │
Client B ──ws://──►│       ▼                            │
                    │  Message Broker ("/topic")         │
Client C ──ws://──►│       │                            │
                    │       ├─► /topic/orders/new        │
                    │       ├─► /topic/orders/status      │
                    │       ├─► /topic/batches/expiring   │
                    │       └─► /topic/store/{storeId}   │
                    └──────────────────────────────────┘
```

---

## Bước 3: Cập Nhật SecurityConfig

Mở file `SecurityConfig.java` và cập nhật để cho phép WebSocket endpoint đi qua:

```java
package org.luun.kitchencontrolbev1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(request ->
                request
                    // ✅ Cho phép WebSocket endpoint không cần xác thực
                    .requestMatchers("/ws/**").permitAll()
                    .anyRequest().permitAll());

        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }
}
```

> **Lưu ý:** Hiện tại bạn đang `permitAll()` cho mọi request. Khi bật JWT sau này, hãy đảm bảo `/ws/**` vẫn được permit.

---

## Bước 4: Cập Nhật CORS Trong WebConfig

File `WebConfig.java` hiện tại dùng `WebMvcConfigurer` — chỉ áp dụng cho HTTP thường, **KHÔNG** áp dụng cho WebSocket. CORS cho WebSocket đã được cấu hình trong `WebSocketConfig` qua `setAllowedOrigins()`.

**Không cần thay đổi gì ở `WebConfig.java`** — CORS cho WebSocket đã OK ở Bước 2.

---

## Bước 5: Tạo DTO Cho WebSocket Message

Tạo file mới: `src/main/java/org/luun/kitchencontrolbev1/dto/response/WebSocketMessage.java`

```java
package org.luun.kitchencontrolbev1.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO chuẩn cho mọi message gửi qua WebSocket.
 * Giống ApiResponse<T> nhưng dành cho WebSocket.
 * 
 * @param <T> Kiểu dữ liệu payload (OrderResponse, LogBatchResponse, ...)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    /**
     * Loại sự kiện, giúp frontend phân biệt cách xử lý.
     * VD: "ORDER_CREATED", "ORDER_STATUS_CHANGED", "BATCH_EXPIRING", "INVENTORY_UPDATED"
     */
    private String type;

    /**
     * Nội dung thông báo ngắn gọn (hiển thị trên UI).
     * VD: "Đơn hàng #123 đã được tạo", "Lô hàng #456 sắp hết hạn"
     */
    private String message;

    /**
     * Dữ liệu chi tiết đi kèm.
     * VD: OrderResponse, LogBatchResponse, DeliveryResponse, ...
     */
    private T data;

    /**
     * Thời gian sự kiện xảy ra.
     */
    private LocalDateTime timestamp;
}
```

---

## Bước 6: Tạo NotificationService

Đây là **trái tim** của hệ thống real-time — service trung tâm để gửi message đến client.

Tạo file: `src/main/java/org/luun/kitchencontrolbev1/service/NotificationService.java`

```java
package org.luun.kitchencontrolbev1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luun.kitchencontrolbev1.dto.response.WebSocketMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service trung tâm để gửi thông báo real-time qua WebSocket.
 * 
 * SimpMessagingTemplate: Là công cụ chính của Spring để gửi message
 * đến các topic mà client đã subscribe.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // ======================== ORDER NOTIFICATIONS ========================

    /**
     * Thông báo khi có đơn hàng mới được tạo.
     * Gửi đến: Tất cả Thủ kho và Manager đang online.
     */
    public <T> void notifyNewOrder(T orderData) {
        sendToTopic("/topic/orders/new", "ORDER_CREATED", "Có đơn hàng mới!", orderData);
    }

    /**
     * Thông báo khi trạng thái đơn hàng thay đổi.
     * Gửi đến: Tất cả người dùng đang theo dõi đơn hàng.
     */
    public <T> void notifyOrderStatusChanged(T orderData) {
        sendToTopic("/topic/orders/status", "ORDER_STATUS_CHANGED",
                "Trạng thái đơn hàng đã thay đổi", orderData);
    }

    /**
     * Thông báo riêng cho 1 cửa hàng cụ thể.
     * VD: Khi đơn hàng của store đó được cập nhật.
     */
    public <T> void notifyStore(Integer storeId, String type, String message, T data) {
        sendToTopic("/topic/store/" + storeId, type, message, data);
    }

    // ======================== DELIVERY NOTIFICATIONS ========================

    /**
     * Thông báo khi shipper được gán chuyến giao hàng mới.
     */
    public <T> void notifyDeliveryAssigned(T deliveryData) {
        sendToTopic("/topic/deliveries/assigned", "DELIVERY_ASSIGNED",
                "Có chuyến giao hàng mới được gán", deliveryData);
    }

    /**
     * Thông báo riêng cho 1 shipper cụ thể.
     */
    public <T> void notifyShipper(Integer shipperId, String type, String message, T data) {
        sendToTopic("/topic/shipper/" + shipperId, type, message, data);
    }

    /**
     * Thông báo khi chuyến giao hàng bắt đầu.
     */
    public <T> void notifyDeliveryStarted(T deliveryData) {
        sendToTopic("/topic/deliveries/started", "DELIVERY_STARTED",
                "Chuyến giao hàng đã bắt đầu", deliveryData);
    }

    // ======================== RECEIPT NOTIFICATIONS ========================

    /**
     * Thông báo khi phiếu xuất kho được xác nhận.
     */
    public <T> void notifyReceiptConfirmed(T receiptData) {
        sendToTopic("/topic/receipts/confirmed", "RECEIPT_CONFIRMED",
                "Phiếu xuất kho đã được xác nhận", receiptData);
    }

    // ======================== BATCH NOTIFICATIONS ========================

    /**
     * Thông báo khi có lô hàng sắp hết hạn / đã hết hạn.
     */
    public <T> void notifyBatchExpiring(T batchData) {
        sendToTopic("/topic/batches/expiring", "BATCH_EXPIRING",
                "Cảnh báo: Có lô hàng hết hạn!", batchData);
    }

    // ======================== INVENTORY NOTIFICATIONS ========================

    /**
     * Thông báo khi tồn kho thay đổi (nhập/xuất).
     */
    public <T> void notifyInventoryUpdated(T inventoryData) {
        sendToTopic("/topic/inventory/updated", "INVENTORY_UPDATED",
                "Tồn kho đã được cập nhật", inventoryData);
    }

    // ======================== PRIVATE HELPER ========================

    /**
     * Method gửi message chung - tất cả các method trên đều gọi vào đây.
     *
     * @param topic   Đường dẫn topic (VD: "/topic/orders/new")
     * @param type    Loại sự kiện (VD: "ORDER_CREATED")
     * @param message Nội dung thông báo
     * @param data    Dữ liệu payload
     */
    private <T> void sendToTopic(String topic, String type, String message, T data) {
        WebSocketMessage<T> wsMessage = WebSocketMessage.<T>builder()
                .type(type)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(topic, wsMessage);
        log.info("📡 WebSocket sent to {}: type={}, message={}", topic, type, message);
    }
}
```

---

## Bước 7: Tích Hợp Vào Các Service Hiện Có

### 7.1. Tích hợp vào `OrderServiceImpl.java`

**Thay đổi cần làm:** Inject `NotificationService` và gọi nó sau mỗi thao tác.

```java
// File: OrderServiceImpl.java

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    // ✅ THÊM DÒNG NÀY
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // ... (giữ nguyên code hiện tại) ...

        Order savedOrder = orderRepository.save(order);
        OrderResponse response = mapToResponse(savedOrder);

        // ✅ THÊM: Gửi thông báo real-time khi đơn hàng mới được tạo
        notificationService.notifyNewOrder(response);

        // ✅ THÊM: Gửi thông báo riêng cho store
        notificationService.notifyStore(
            savedOrder.getStore().getStoreId(),
            "ORDER_CREATED",
            "Đơn hàng #" + savedOrder.getOrderId() + " đã được tạo thành công",
            response
        );

        return response;
    }

    @Override
    public OrderResponse updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        OrderResponse response = mapToResponse(updatedOrder);

        // ✅ THÊM: Gửi thông báo real-time khi trạng thái thay đổi
        notificationService.notifyOrderStatusChanged(response);

        // ✅ THÊM: Gửi thông báo riêng cho store của đơn hàng
        if (updatedOrder.getStore() != null) {
            notificationService.notifyStore(
                updatedOrder.getStore().getStoreId(),
                "ORDER_STATUS_CHANGED",
                "Đơn hàng #" + orderId + " chuyển sang " + status.name(),
                response
            );
        }

        return response;
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Integer orderId) {
        // ... (giữ nguyên logic kiểm tra) ...
        order.setStatus(OrderStatus.DONE);
        OrderResponse response = mapToResponse(orderRepository.save(order));

        // ✅ THÊM: Gửi thông báo hoàn thành đơn hàng
        notificationService.notifyOrderStatusChanged(response);

        return response;
    }

    // ... (giữ nguyên các method khác) ...
}
```

### 7.2. Tích hợp vào `DeliveryServiceImpl.java`

```java
// File: DeliveryServiceImpl.java

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailFillService orderDetailFillService;

    // ✅ THÊM DÒNG NÀY
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DeliveryResponse assignShipperToDelivery(AssignShipperRequest request) {
        // ... (giữ nguyên code hiện tại) ...

        delivery.setOrders(orders);
        DeliveryResponse response = mapToResponse(delivery);

        // ✅ THÊM: Thông báo cho tất cả
        notificationService.notifyDeliveryAssigned(response);

        // ✅ THÊM: Thông báo riêng cho shipper được gán
        notificationService.notifyShipper(
            request.getShipperId(),
            "DELIVERY_ASSIGNED",
            "Bạn được gán chuyến giao hàng #" + delivery.getDeliveryId(),
            response
        );

        return response;
    }

    @Override
    @Transactional
    public DeliveryResponse startDelivery(Integer deliveryId) {
        // ... (giữ nguyên code hiện tại) ...
        DeliveryResponse response = mapToResponse(deliveryRepository.save(delivery));

        // ✅ THÊM: Thông báo chuyến giao hàng bắt đầu
        notificationService.notifyDeliveryStarted(response);

        // ✅ THÊM: Thông báo cho từng store trong chuyến
        for (Order order : orders) {
            if (order.getStore() != null) {
                notificationService.notifyStore(
                    order.getStore().getStoreId(),
                    "ORDER_DELIVERING",
                    "Đơn hàng #" + order.getOrderId() + " đang được giao đến bạn",
                    null
                );
            }
        }

        return response;
    }
}
```

### 7.3. Tích hợp vào `ReceiptServiceImpl.java`

```java
// File: ReceiptServiceImpl.java

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    // ... (giữ nguyên các field hiện tại) ...

    // ✅ THÊM DÒNG NÀY
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void confirmReceipt(List<Integer> receiptId) {
        // ... (giữ nguyên code hiện tại) ...

        for (Receipt receipt : receipts) {
            // ... (giữ nguyên logic xử lý) ...

            receiptRepository.save(receipt);

            // ✅ THÊM: Thông báo phiếu xuất kho đã xác nhận
            notificationService.notifyReceiptConfirmed(mapToResponse(receipt));

            // ✅ THÊM: Thông báo tồn kho thay đổi
            notificationService.notifyInventoryUpdated("Tồn kho đã được cập nhật sau xuất kho");
        }
    }
}
```

### 7.4. Tích hợp vào `LogBatchServiceImpl.java`

```java
// File: LogBatchServiceImpl.java

@Service
@Slf4j
@RequiredArgsConstructor
public class LogBatchServiceImpl implements LogBatchService {
    // ... (giữ nguyên các field hiện tại) ...

    // ✅ THÊM DÒNG NÀY
    private final NotificationService notificationService;

    /**
     * Scheduled Job kiểm tra lô hàng hết hạn hàng ngày.
     * ✅ Bây giờ sẽ gửi thông báo real-time cho thủ kho!
     */
    @Scheduled(cron = "0 1 0 * * ?", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void updateExpiredBatches() {
        log.warn("Checking for expired batches...");
        LocalDate today = LocalDate.now();

        List<LogBatch> expiredBatches = logBatchRepository.findByExpiryDateBeforeAndStatusIn(
                today, List.of(LogBatchStatus.DONE.name())
        );

        if (expiredBatches.isEmpty()) {
            log.warn("No expired batches found.");
            return;
        }

        for (LogBatch batch : expiredBatches) {
            batch.setStatus(LogBatchStatus.WAITING_TO_CANCLE);
            logBatchRepository.save(batch);
            log.warn("Batch ID " + batch.getBatchId() + " has expired.");

            // ✅ THÊM: Gửi thông báo real-time cho thủ kho
            notificationService.notifyBatchExpiring(mapToResponse(batch));
        }

        // ✅ THÊM: Thông báo tổng hợp
        notificationService.notifyBatchExpiring(
            "⚠️ Có " + expiredBatches.size() + " lô hàng hết hạn cần xử lý!"
        );
    }
}
```

---

## Bước 8: Tạo WebSocketController (Tùy Chọn)

File này xử lý khi **client gửi message lên server** qua WebSocket (ít dùng nhưng cần cho một số case).

Tạo file: `src/main/java/org/luun/kitchencontrolbev1/controller/WebSocketController.java`

```java
package org.luun.kitchencontrolbev1.controller;

import lombok.RequiredArgsConstructor;
import org.luun.kitchencontrolbev1.dto.response.WebSocketMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * Controller xử lý message từ client gửi lên qua WebSocket.
 * Chỉ cần khi client muốn gửi data lên server qua WS (không phải REST).
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    /**
     * Client gửi message đến "/app/ping" → Server phản hồi "/topic/pong".
     * Dùng để test kết nối WebSocket.
     */
    @MessageMapping("/ping")    // Client gửi đến: /app/ping
    @SendTo("/topic/pong")      // Server phản hồi đến: /topic/pong
    public WebSocketMessage<String> handlePing(String message) {
        return WebSocketMessage.<String>builder()
                .type("PONG")
                .message("Server nhận được: " + message)
                .data("pong")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

---

## Bước 9: Kết Nối Từ Frontend (ReactJS)

### 9.1. Cài đặt thư viện

```bash
npm install @stomp/stompjs sockjs-client
```

### 9.2. Tạo WebSocket Service

```javascript
// File: src/services/websocketService.js

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const SOCKET_URL = 'http://localhost:8080/ws'; // Hoặc URL deploy trên Render

let stompClient = null;

/**
 * Kết nối WebSocket đến server.
 * @param {Function} onConnected - Callback khi kết nối thành công
 * @param {Function} onError - Callback khi có lỗi
 */
export const connectWebSocket = (onConnected, onError) => {
    stompClient = new Client({
        // Dùng SockJS để tương thích trình duyệt cũ
        webSocketFactory: () => new SockJS(SOCKET_URL),

        // Tự động reconnect sau 5 giây nếu mất kết nối
        reconnectDelay: 5000,

        // Debug log (tắt khi lên production)
        debug: (str) => console.log('[WS]', str),

        onConnect: () => {
            console.log('✅ WebSocket Connected!');
            if (onConnected) onConnected();
        },

        onStompError: (frame) => {
            console.error('❌ WebSocket Error:', frame);
            if (onError) onError(frame);
        },
    });

    stompClient.activate();
};

/**
 * Đăng ký nhận message từ một topic.
 * @param {string} topic - Đường dẫn topic (VD: '/topic/orders/new')
 * @param {Function} callback - Hàm xử lý khi nhận message
 * @returns {Object} subscription - Dùng để unsubscribe sau
 */
export const subscribeToTopic = (topic, callback) => {
    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket chưa kết nối!');
        return null;
    }

    return stompClient.subscribe(topic, (message) => {
        const body = JSON.parse(message.body);
        callback(body);
    });
};

/**
 * Gửi message lên server qua WebSocket.
 * @param {string} destination - Đường dẫn (VD: '/app/ping')
 * @param {Object} body - Dữ liệu gửi đi
 */
export const sendMessage = (destination, body) => {
    if (!stompClient || !stompClient.connected) {
        console.error('WebSocket chưa kết nối!');
        return;
    }

    stompClient.publish({
        destination: destination,
        body: JSON.stringify(body),
    });
};

/**
 * Ngắt kết nối WebSocket.
 */
export const disconnectWebSocket = () => {
    if (stompClient) {
        stompClient.deactivate();
        console.log('🔌 WebSocket Disconnected');
    }
};
```

### 9.3. Sử dụng trong React Component

```jsx
// File: src/pages/OrderManagement.jsx (Ví dụ cho trang quản lý đơn hàng)

import React, { useEffect, useState } from 'react';
import { connectWebSocket, subscribeToTopic, disconnectWebSocket } from '../services/websocketService';
import { toast } from 'react-toastify'; // Hoặc thư viện notification khác

const OrderManagement = () => {
    const [orders, setOrders] = useState([]);

    useEffect(() => {
        // 1. Kết nối WebSocket khi component mount
        connectWebSocket(
            () => {
                // 2. Khi đã kết nối, subscribe các topic cần thiết

                // Nhận thông báo đơn hàng mới
                subscribeToTopic('/topic/orders/new', (message) => {
                    console.log('📦 Đơn hàng mới:', message);
                    toast.success(message.message);
                    // Thêm đơn hàng mới vào danh sách (không cần gọi API lại!)
                    setOrders(prev => [message.data, ...prev]);
                });

                // Nhận thông báo thay đổi trạng thái đơn hàng
                subscribeToTopic('/topic/orders/status', (message) => {
                    console.log('🔄 Trạng thái thay đổi:', message);
                    toast.info(message.message);
                    // Cập nhật trạng thái đơn hàng trong danh sách
                    setOrders(prev => prev.map(order =>
                        order.orderId === message.data.orderId
                            ? message.data
                            : order
                    ));
                });
            },
            (error) => {
                console.error('WebSocket connection error:', error);
                toast.error('Mất kết nối real-time. Đang thử kết nối lại...');
            }
        );

        // 3. Cleanup khi component unmount
        return () => disconnectWebSocket();
    }, []);

    return (
        <div>
            <h1>Quản Lý Đơn Hàng (Real-time)</h1>
            {orders.map(order => (
                <div key={order.orderId}>
                    <p>Đơn #{order.orderId} - {order.status}</p>
                </div>
            ))}
        </div>
    );
};

export default OrderManagement;
```

### 9.4. Subscribe Cho Từng Store / Shipper

```jsx
// Cho cửa hàng cụ thể (storeId lấy từ JWT token sau khi login)
useEffect(() => {
    connectWebSocket(() => {
        const storeId = getUserStoreId(); // Lấy từ JWT token
        subscribeToTopic(`/topic/store/${storeId}`, (message) => {
            toast.info(message.message);
        });
    });
    return () => disconnectWebSocket();
}, []);

// Cho shipper cụ thể
useEffect(() => {
    connectWebSocket(() => {
        const userId = getUserId(); // Lấy từ JWT token
        subscribeToTopic(`/topic/shipper/${userId}`, (message) => {
            toast.info(message.message);
        });
    });
    return () => disconnectWebSocket();
}, []);
```

---

## Bước 10: Kiểm Thử

### 10.1. Test bằng Postman (WebSocket)

1. Mở Postman → **New** → **WebSocket**
2. URL: `ws://localhost:8080/ws`
3. Protocol: STOMP
4. Gửi CONNECT frame:
   ```
   CONNECT
   accept-version:1.1,1.0
   heart-beat:10000,10000

   ^@
   ```
5. Subscribe:
   ```
   SUBSCRIBE
   id:sub-0
   destination:/topic/orders/new

   ^@
   ```

### 10.2. Test bằng Online Tool

Truy cập: https://jxy.me/websocket-debug-tool/
- URL: `http://localhost:8080/ws` (dùng SockJS)
- Subscribe: `/topic/orders/new`

### 10.3. Test End-to-End

1. **Mở 2 tab trình duyệt** với frontend
2. **Tab 1** subscribe `/topic/orders/new`
3. **Tab 2** (hoặc Postman) gọi `POST /orders` tạo đơn hàng
4. ✅ **Tab 1** sẽ tự động nhận thông báo mà **không cần refresh**

---

## Kiến Trúc Tổng Quan

```
┌─────────────────┐     ┌──────────────────────────────────────────────┐
│  React Frontend │     │           Spring Boot Backend                │
│                 │     │                                              │
│  ┌───────────┐  │ WS  │  ┌────────────────┐   ┌──────────────────┐  │
│  │ SockJS +  │◄─┼─────┼──│ /ws Endpoint   │   │  REST Controllers│  │
│  │ STOMP.js  │  │     │  │ (WebSocketCfg) │   │  (OrderCtrl,     │  │
│  └─────┬─────┘  │     │  └────────────────┘   │   DeliveryCtrl,  │  │
│        │        │     │          │              │   ReceiptCtrl)   │  │
│        │        │     │          ▼              └────────┬─────────┘  │
│        │        │     │  ┌────────────────┐             │            │
│        ▼        │     │  │ Message Broker │◄────────────┤            │
│  ┌───────────┐  │     │  │  /topic/*      │   ┌────────┴─────────┐  │
│  │ Subscribe │  │     │  └────────────────┘   │   Service Layer  │  │
│  │ /topic/   │  │     │                       │   (OrderService, │  │
│  │ orders/   │  │     │                       │    + Notification │  │
│  │ new       │  │     │                       │      Service)    │  │
│  └───────────┘  │     │                       └──────────────────┘  │
└─────────────────┘     └──────────────────────────────────────────────┘
```

---

## Xử Lý Lỗi & Best Practices

### ✅ Nên làm

1. **Luôn dùng `try-catch`** khi gửi WebSocket message trong service:
   ```java
   try {
       notificationService.notifyNewOrder(response);
   } catch (Exception e) {
       log.error("Failed to send WebSocket notification", e);
       // Không throw - WebSocket fail không ảnh hưởng business logic
   }
   ```

2. **Reconnect tự động** ở frontend (đã có trong config `reconnectDelay: 5000`)

3. **Cleanup subscription** khi component unmount để tránh memory leak

4. **Log tất cả message** gửi đi (đã có trong `NotificationService`)

### ❌ Không nên

1. **KHÔNG** gửi dữ liệu quá lớn qua WebSocket (giới hạn payload)
2. **KHÔNG** gửi thông tin nhạy cảm (password, token) qua WebSocket topic public
3. **KHÔNG** phụ thuộc 100% vào WebSocket — luôn có fallback bằng REST API

### 🔒 Bảo mật nâng cao (Làm sau)

Khi bạn bật JWT xác thực, cần thêm `ChannelInterceptor` để xác thực WebSocket:

```java
// File: WebSocketAuthInterceptor.java (Làm sau khi cần)
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            // Xác thực JWT token ở đây
        }
        return message;
    }
}
```

---

## 📁 Tóm Tắt Các File Cần Tạo / Chỉnh Sửa

| # | File | Hành động | Mục đích |
|---|------|-----------|----------|
| 1 | `pom.xml` | ✏️ Sửa | Thêm `spring-boot-starter-websocket` |
| 2 | `WebSocketConfig.java` | 🆕 Tạo mới | Cấu hình STOMP endpoint + broker |
| 3 | `SecurityConfig.java` | ✏️ Sửa | Cho phép `/ws/**` |
| 4 | `WebSocketMessage.java` | 🆕 Tạo mới | DTO cho message WebSocket |
| 5 | `NotificationService.java` | 🆕 Tạo mới | Service trung tâm gửi thông báo |
| 6 | `OrderServiceImpl.java` | ✏️ Sửa | Thêm gửi thông báo |
| 7 | `DeliveryServiceImpl.java` | ✏️ Sửa | Thêm gửi thông báo |
| 8 | `ReceiptServiceImpl.java` | ✏️ Sửa | Thêm gửi thông báo |
| 9 | `LogBatchServiceImpl.java` | ✏️ Sửa | Thêm gửi thông báo |
| 10 | `WebSocketController.java` | 🆕 Tạo mới (tùy chọn) | Xử lý message từ client |
| 11 | Frontend `websocketService.js` | 🆕 Tạo mới | Kết nối WS từ React |

---

> **💡 Mẹo:** Bắt đầu với Bước 1-6, test ping/pong trước, rồi mới tích hợp vào từng service (Bước 7).  
> Nếu gặp lỗi, kiểm tra console log ở cả backend và frontend.
