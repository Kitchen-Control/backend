# API Documentation: Step-by-Step Guide to Creating the Order API

This document provides a detailed, easy-to-understand walkthrough for creating the API endpoints to manage `Orders` in the Kitchen Control Spring Boot application. We will follow best practices, using a layered architecture (Controller, Service, Repository) and Data Transfer Objects (DTOs).

## Prerequisites
- Java 17 or newer
- Apache Maven
- A running database instance (like PostgreSQL or MySQL)
- Basic knowledge of Spring Boot and JPA (Java Persistence API)

---

## Folder Structure
For clarity, here is the target folder structure for the new files we will create within `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/`:

```
kitchencontrolbe
├── KItchenControlBeApplication.java
├── controllers
│   └── OrderController.java
├── models
│   ├── Order.java
│   └── OrderDetail.java
│   └── enums
│       └── OrderStatus.java
├── repositories
│   └── OrderRepository.java
│   └── OrderDetailRepository.java
├── services
│   ├── IOrderService.java
│   └── impl
│       └── OrderServiceImpl.java
└── dtos
    ├── OrderRequestDTO.java
    └── OrderResponseDTO.java
```

---

## Step 1: Define the Database Models (Entities)

First, we need to map our database tables (`orders` and `order_details`) to Java objects. These are called "Entities".

#### 1.1. Create the Order Status Enum
It's good practice to represent status fields as Enums.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/models/enums/OrderStatus.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.models.enums;

public enum OrderStatus {
    WAITTING,
    PROCESSING,
    DONE,
    DAMAGED
}
```

#### 1.2. Create the `Order` Entity
This class maps to the `orders` table.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/models/Order.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.models;

import jakarta.persistence.*;
import lombok.Data;
import org.luun.hsf.asm01.kitchencontrolbe.models.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    // We will map relationships later for simplicity. For now, we use simple IDs.
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @Column(name = "store_id", nullable = false)
    private int storeId;

    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}
```

#### 1.3. Create the `OrderDetail` Entity
This class maps to the `order_details` table.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/models/OrderDetail.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_details")
@Data
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private int orderDetailId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private int productId;

    @Column(name = "quantity", nullable = false)
    private float quantity;
}
```
*Note: We are using `@Data` from Lombok to reduce boilerplate code. Ensure Lombok is in your `pom.xml`.*

---

## Step 2: Create the Repository Layer
Repositories are interfaces that provide methods to perform database operations on our entities. Spring Data JPA will automatically implement them for us.

#### 2.1. Create `OrderRepository`
**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/repositories/OrderRepository.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.repositories;

import org.luun.hsf.asm01.kitchencontrolbe.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
```

#### 2.2. Create `OrderDetailRepository`
**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/repositories/OrderDetailRepository.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.repositories;

import org.luun.hsf.asm01.kitchencontrolbe.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}
```

---

## Step 3: Define Data Transfer Objects (DTOs)
DTOs are used to shape the data sent to and from the API. This decouples the API structure from the database structure, which is a crucial security and design principle.

#### 3.1. Create `OrderRequestDTO`
This DTO represents the JSON payload the client will send to create an order.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/dtos/OrderRequestDTO.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.dtos;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequestDTO {
    private int storeId;
    private List<OrderDetailRequest> orderDetails;

    @Data
    public static class OrderDetailRequest {
        private int productId;
        private float quantity;
    }
}
```

#### 3.2. Create `OrderResponseDTO`
This DTO represents the JSON payload the server will send back as a response.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/dtos/OrderResponseDTO.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.dtos;

import lombok.Builder;
import lombok.Data;
import org.luun.hsf.asm01.kitchencontrolbe.models.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private int orderId;
    private int storeId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private List<OrderDetailResponse> orderDetails;

    @Data
    @Builder
    public static class OrderDetailResponse {
        private int productId;
        private float quantity;
    }
}
```

---

## Step 4: Create the Service Layer (Business Logic)
The service layer contains the core business logic. It uses the repository to interact with the database and is called by the controller.

#### 4.1. Create the Service Interface
**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/services/IOrderService.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.services;

import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderResponseDTO;
import java.util.List;

public interface IOrderService {
    OrderResponseDTO createOrder(OrderRequestDTO request);
    OrderResponseDTO getOrderById(int orderId);
    List<OrderResponseDTO> getAllOrders();
}
```

#### 4.2. Create the Service Implementation
**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/services/impl/OrderServiceImpl.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.services.impl;

import lombok.RequiredArgsConstructor;
import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderRequestDTO;
import org.luun.hsf.asm01.kitchencontrolbe.dtos.OrderResponseDTO;
import org.luun.hsf.asm01.kitchencontrolbe.models.Order;
import org.luun.hsf.asm01.kitchencontrolbe.models.OrderDetail;
import org.luun.hsf.asm01.kitchencontrolbe.models.enums.OrderStatus;
import org.luun.hsf.asm01.kitchencontrolbe.repositories.OrderRepository;
import org.luun.hsf.asm01.kitchencontrolbe.services.IOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Automatically injects dependencies via constructor
public class OrderServiceImpl implements IOrderService {

    private final OrderRepository orderRepository;
    // Inject other repositories if needed, e.g., for validation
    // private final StoreRepository storeRepository;
    // private final ProductRepository productRepository;

    @Override
    @Transactional // Ensures the whole method runs in a single database transaction
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        // Here you could add validation logic:
        // - Check if storeId exists
        // - Check if all productIds exist and are available

        Order order = new Order();
        order.setStoreId(request.getStoreId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITTING); // Default status

        List<OrderDetail> details = request.getOrderDetails().stream().map(detailDto -> {
            OrderDetail detail = new OrderDetail();
            detail.setProductId(detailDto.getProductId());
            detail.setQuantity(detailDto.getQuantity());
            detail.setOrder(order); // Link detail to the order
            return detail;
        }).collect(Collectors.toList());

        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order);
        return mapToResponseDTO(savedOrder);
    }

    @Override
    public OrderResponseDTO getOrderById(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return mapToResponseDTO(order);
    }
    
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Helper method to map an Order entity to an OrderResponseDTO
    private OrderResponseDTO mapToResponseDTO(Order order) {
        List<OrderResponseDTO.OrderDetailResponse> detailResponses = order.getOrderDetails().stream()
            .map(detail -> OrderResponseDTO.OrderDetailResponse.builder()
                .productId(detail.getProductId())
                .quantity(detail.getQuantity())
                .build())
            .collect(Collectors.toList());

        return OrderResponseDTO.builder()
            .orderId(order.getOrderId())
            .storeId(order.getStoreId())
            .orderDate(order.getOrderDate())
            .status(order.getStatus())
            .orderDetails(detailResponses)
            .build();
    }
}
```

---

## Step 5: Create the Controller Layer (API Endpoints)
The controller is the entry point for API requests. It handles HTTP requests, calls the appropriate service method, and returns a response.

**File:** `src/main/java/org/luun/hsf/asm01/kitchencontrolbe/controllers/OrderController.java`
```java
package org.luun.hsf.asm01.kitchencontrolbe.controllers;

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
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO request) {
        OrderResponseDTO newOrder = orderService.createOrder(request);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable("id") int orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
    
    // Future endpoints like updateStatus can be added here
    /*
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable("id") int orderId, @RequestParam("status") String status) {
        // Implementation in service layer
        return ResponseEntity.ok().build();
    }
    */
}
```

---

## Step 6: Run and Test the API

1.  **Run the Application:** Start your Spring Boot application.
2.  **Test with a REST Client:** Use a tool like Postman, Insomnia, or `curl` to test the `createOrder` endpoint.

**Endpoint:** `POST http://localhost:8080/api/orders`
**Headers:** `Content-Type: application/json`

**Example JSON Body:**
```json
{
  "storeId": 1,
  "orderDetails": [
    {
      "productId": 101,
      "quantity": 10.5
    },
    {
      "productId": 102,
      "quantity": 5.0
    }
  ]
}
```

**Expected Success Response (Status `201 Created`):**
```json
{
    "orderId": 1,
    "storeId": 1,
    "orderDate": "2026-01-19T10:30:00.123456", // Example date
    "status": "WAITTING",
    "orderDetails": [
        {
            "productId": 101,
            "quantity": 10.5
        },
        {
            "productId": 102,
            "quantity": 5.0
        }
    ]
}
```

You have now successfully created a robust and scalable API for managing orders!
