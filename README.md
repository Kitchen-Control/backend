<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen?style=for-the-badge&logo=spring-boot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk" alt="Java"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License"/>
</p>

# 🍳 Kitchen Control — Central Kitchen Management System

> **Backend API** cho hệ thống quản lý bếp trung tâm (Central Kitchen), phục vụ quy trình vận hành từ **đặt hàng → sản xuất → quản lý kho → xuất kho → giao hàng → nhận hàng** cho chuỗi cửa hàng.

---

## 📋 Mục Lục

- [Tổng Quan](#-tổng-quan)
- [Tính Năng Chính](#-tính-năng-chính)
- [Tech Stack](#-tech-stack)
- [Kiến Trúc Hệ Thống](#-kiến-trúc-hệ-thống)
- [Cấu Trúc Dự Án](#-cấu-trúc-dự-án)
- [Database Schema](#-database-schema)
- [API Documentation](#-api-documentation)
- [Business Flow](#-business-flow)
- [Cài Đặt & Chạy](#-cài-đặt--chạy)
- [Cấu Hình Môi Trường](#-cấu-hình-môi-trường)
- [Deployment](#-deployment)
- [Đóng Góp](#-đóng-góp)

---

## 🎯 Tổng Quan

**Kitchen Control** là hệ thống quản lý vận hành bếp trung tâm, nơi sản phẩm được sản xuất tập trung và phân phối đến các cửa hàng chi nhánh. Hệ thống hỗ trợ toàn bộ quy trình supply chain nội bộ:

```
Cửa hàng đặt hàng → Bếp trung tâm xử lý → Thủ kho xuất kho → Shipper giao hàng → Cửa hàng nhận
```

### Các vai trò người dùng (Roles)

| Vai trò | Mô tả |
|---------|-------|
| **Manager** | Quản lý tổng thể: duyệt đơn hàng, lên kế hoạch sản xuất, theo dõi báo cáo |
| **Warehouse Keeper** | Thủ kho: quản lý tồn kho, tạo phiếu xuất, xác nhận xuất kho, xử lý hàng hết hạn |
| **Store Owner** | Chủ cửa hàng: đặt hàng, theo dõi đơn hàng, phản hồi chất lượng |
| **Shipper** | Người giao hàng: nhận chuyến, giao hàng, xác nhận hoàn thành |

---

## ✨ Tính Năng Chính

### 📦 Quản Lý Đơn Hàng (Order Management)
- Tạo đơn hàng từ cửa hàng chi nhánh
- Theo dõi trạng thái đơn hàng theo thời gian thực
- Lọc đơn hàng theo trạng thái, cửa hàng, shipper

### 🏭 Kế Hoạch Sản Xuất (Production Planning)
- Lên kế hoạch sản xuất theo ngày/tuần
- Quản lý chi tiết kế hoạch theo từng sản phẩm
- Liên kết kế hoạch với lô sản xuất (Log Batch)

### 📋 Quản Lý Công Thức (Recipe Management)
- Quản lý BOM (Bill of Materials) — công thức sản xuất
- Mỗi sản phẩm thành phẩm có công thức với danh sách nguyên liệu thô
- Tìm kiếm công thức theo tên

### 🏪 Quản Lý Kho (Inventory Management)
- Theo dõi tồn kho theo sản phẩm và lô hàng
- Tính toán **Available Stock** (tồn kho khả dụng = tồn kho thực tế − đơn hàng đang chờ)
- Quản lý nhập/xuất kho với lịch sử giao dịch đầy đủ
- Thuật toán **FEFO** (First Expired, First Out) — ưu tiên xuất hàng sắp hết hạn trước

### 📜 Quản Lý Lô Hàng (Batch Tracking)
- Truy xuất nguồn gốc từng lô hàng (sản xuất hoặc mua ngoài)
- Tự động phát hiện lô hàng hết hạn (Scheduled Job — chạy mỗi ngày 00:01)
- Quy trình xử lý hủy hàng hết hạn

### 🧾 Phiếu Xuất Kho (Receipt / Export)
- Tạo phiếu xuất kho nháp (Draft)
- Xác nhận xuất kho → tự động trừ kho + ghi log giao dịch
- Liên kết phiếu xuất với đơn hàng

### 🚚 Quản Lý Giao Hàng (Delivery Management)
- Gán shipper cho chuyến giao hàng
- Gom nhiều đơn hàng vào 1 chuyến giao
- Theo dõi trạng thái giao hàng: `PROCESSING → DISPATCHED → DELIVERING → DONE`

### ⭐ Phản Hồi Chất Lượng (Quality Feedback)
- Cửa hàng đánh giá chất lượng sản phẩm sau khi nhận hàng
- Rating + Comment cho từng đơn hàng

### 🔐 Xác Thực & Phân Quyền
- Xác thực bằng **JWT (JSON Web Token)** — thuật toán HS512
- Token chứa: userId, fullName, roleName, storeId, storeName
- Hỗ trợ introspect token (kiểm tra token hợp lệ)

---

## 🛠 Tech Stack

| Công nghệ | Phiên bản | Mục đích |
|-----------|----------|----------|
| **Java** | 17 | Ngôn ngữ chính |
| **Spring Boot** | 4.0.1 | Framework chính |
| **Spring Data JPA** | — | ORM, tương tác database |
| **Spring Security** | — | Bảo mật, xác thực |
| **Spring OAuth2 Resource Server** | — | JWT-based authentication |
| **PostgreSQL** | — | Cơ sở dữ liệu chính |
| **Lombok** | — | Giảm boilerplate code |
| **SpringDoc OpenAPI** | 3.0.1 | Swagger UI — API documentation |
| **Nimbus JOSE+JWT** | — | Tạo & xác thực JWT token |
| **Maven** | — | Build tool & dependency management |
| **Docker** | — | Containerization |

---

## 🏗 Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ React Web    │  │ Mobile App   │  │ Swagger UI   │       │
│  │ (Vercel)     │  │ (Future)     │  │ /swagger-ui  │       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       │
└─────────┼─────────────────┼─────────────────┼───────────────┘
          │ HTTP/REST       │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                       │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 Controller Layer                    │    │
│  │  AuthCtrl · OrderCtrl · DeliveryCtrl · ReceiptCtrl  │    │
│  │  ProductCtrl · InventoryCtrl · LogBatchCtrl · ...   │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                   │
│  ┌──────────────────────▼──────────────────────────────┐    │
│  │                  Service Layer                      │    │
│  │  AuthService · OrderService · DeliveryService       │    │
│  │  InventoryService · LogBatchService · ReceiptService│    │
│  │  + Scheduled Jobs (Batch Expiry Check)              │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                   │
│  ┌──────────────────────▼──────────────────────────────┐    │
│  │               Repository Layer (JPA)                │    │
│  │  17 Repositories — Spring Data JPA                  │    │
│  └──────────────────────┬──────────────────────────────┘    │
│                         │                                   │
│  ┌──────────────────────▼──────────────────────────────┐    │
│  │                  Config Layer                       │    │
│  │  SecurityConfig · WebConfig (CORS)                  │    │
│  └─────────────────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────┘
                            │ JDBC
                            ▼
                  ┌─────────────────────┐
                  │    PostgreSQL DB    │
                  │   (Render.com)      │
                  └─────────────────────┘
```

---

## 📂 Cấu Trúc Dự Án

```
backend/
├── 📄 pom.xml                          # Maven dependencies & build config
├── 🐳 Dockerfile                       # Multi-stage Docker build
├── 📁 docs/                            # Tài liệu hướng dẫn
│   ├── ENTITY_CREATION_GUIDE.md
│   ├── JWT_SPRING_SECURITY_GUIDE.md
│   ├── LOGIN_FEATURE_GUIDE.md
│   └── WEBSOCKET_REALTIME_GUIDE.md
│
└── 📁 src/main/java/org/luun/kitchencontrolbev1/
    │
    ├── 🚀 KitchenControlBEv1Application.java    # Main entry point
    │
    ├── 📁 config/                                # Cấu hình
    │   ├── SecurityConfig.java                   #   Spring Security & JWT
    │   └── WebConfig.java                        #   CORS configuration
    │
    ├── 📁 controller/                            # REST API Controllers
    │   ├── AuthController.java                   #   POST /auth/login, /auth/v2/login
    │   ├── OrderController.java                  #   CRUD /orders
    │   ├── DeliveryController.java               #   /deliveries
    │   ├── ReceiptController.java                #   /receipts
    │   ├── ProductController.java                #   /products
    │   ├── InventoryController.java              #   /inventories
    │   ├── InventoryTransactionController.java   #   /inventory-transactions
    │   ├── LogBatchController.java               #   /log-batches
    │   ├── ProductionPlanController.java         #   /production-plans
    │   ├── ProductionPlanDetailController.java   #   /production-plan-details
    │   ├── RecipeController.java                 #   /recipes
    │   ├── RecipeDetailController.java           #   /recipe-details
    │   ├── UserController.java                   #   /users
    │   ├── StoreController.java                  #   /stores
    │   ├── QualityFeedbackController.java        #   /feedbacks
    │   ├── OrderDetailController.java            #   /order-details
    │   └── OrderDetailFillController.java        #   /order-detail-fills
    │
    ├── 📁 dto/
    │   ├── 📁 request/                           # Incoming DTOs
    │   │   ├── LoginRequest.java
    │   │   ├── AuthenticationRequest.java
    │   │   ├── OrderRequest.java
    │   │   ├── OrderDetailRequest.java
    │   │   ├── ProductRequest.java
    │   │   ├── LogBatchRequest.java
    │   │   ├── ProductionPlanRequest.java
    │   │   ├── AssignShipperRequest.java
    │   │   └── ...
    │   └── 📁 response/                          # Outgoing DTOs
    │       ├── ApiResponse.java                  #   Generic wrapper <T>
    │       ├── AuthenticationResponse.java
    │       ├── OrderResponse.java
    │       ├── InventoryResponse.java
    │       └── ...
    │
    ├── 📁 entity/                                # JPA Entities (15 tables)
    │   ├── User.java
    │   ├── Role.java
    │   ├── Store.java
    │   ├── Product.java
    │   ├── Recipe.java & RecipeDetail.java
    │   ├── Order.java & OrderDetail.java & OrderDetailFill.java
    │   ├── Inventory.java & InventoryTransaction.java
    │   ├── LogBatch.java
    │   ├── ProductionPlan.java & ProductionPlanDetail.java
    │   ├── Delivery.java
    │   ├── Receipt.java
    │   ├── QualityFeedback.java
    │   └── Report.java
    │
    ├── 📁 enums/                                 # Enum definitions
    │   ├── OrderStatus.java                      #   WAITTING → PROCESSING → DISPATCHED → DELIVERING → DONE
    │   ├── LogBatchStatus.java                   #   PROCESSING → DONE → WAITING_TO_CANCLE → DAMAGED
    │   ├── ReceiptStatus.java                    #   DRAFT → COMPLETED → CANCELLED
    │   ├── InventoryTransactionType.java         #   IMPORT, EXPORT
    │   ├── ProductType.java                      #   RAW_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT
    │   ├── LogBatchType.java                     #   PRODUCTION, PURCHASE
    │   └── ErrorCode.java
    │
    ├── 📁 exception/
    │   └── GlobalHandlerException.java           # @ControllerAdvice
    │
    ├── 📁 repository/                            # Spring Data JPA Repos (17)
    │   └── ...
    │
    └── 📁 service/
        ├── AuthService.java, OrderService.java, ... (17 interfaces)
        └── 📁 impl/                              # Service implementations
            ├── AuthServiceImpl.java              #   JWT generation, login
            ├── OrderServiceImpl.java             #   Order CRUD, status flow
            ├── DeliveryServiceImpl.java          #   Assign shipper, start delivery
            ├── ReceiptServiceImpl.java           #   Create receipt, confirm export
            ├── InventoryServiceImpl.java         #   Available stock, FEFO deduction
            ├── LogBatchServiceImpl.java          #   Batch tracking, expiry scheduler
            └── ...
```

---

## 🗄 Database Schema

### Entity Relationship Diagram

```
┌──────────┐     ┌──────────┐     ┌────────────────┐
│  roles   │1───*│  users   │1───1│    stores      │
│──────────│     │──────────│     │────────────────│
│ role_id  │     │ user_id  │     │ store_id       │
│ role_name│     │ username │     │ store_name     │
└──────────┘     │ password │     │ address        │
                 │ full_name│     │ phone          │
                 └────┬─────┘     └───────┬────────┘
                      │                   │
                      │ 1:N               │ 1:N
                      ▼                   ▼
               ┌──────────────┐    ┌──────────────┐
               │  deliveries  │    │   orders     │
               │──────────────│    │──────────────│
               │ delivery_id  │1──*│ order_id     │
               │ delivery_date│    │ order_date   │
               │ shipper_id   │    │ status       │
               └──────────────┘    │ store_id     │
                                   │ delivery_id  │
                                   └──────┬───────┘
                                          │ 1:N
                                          ▼
                                   ┌──────────────────┐
                                   │  order_details   │
                                   │──────────────────│
                                   │ order_detail_id  │
                                   │ order_id         │
                                   │ product_id       │──────────┐
                                   │ quantity         │          │
                                   └───────┬──────────┘          │
                                           │ 1:N                 │
                                           ▼                     │
                                   ┌──────────────────┐          │
                                   │ order_detail_fill│          │
                                   │──────────────────│          │
                                   │ fill_id          │          │
                                   │ order_detail_id  │          │
                                   │ batch_id ────────┼────┐     │
                                   │ quantity         │    │     │
                                   └──────────────────┘    │     │
                                                           │     │
┌──────────────────┐    ┌──────────────────┐               │     │
│production_plans  │    │   products       │◄──────────────┼─────┘
│──────────────────│    │──────────────────│               │
│ plan_id          │    │ product_id       │               │
│ plan_date        │    │ product_name     │               │
│ start_date       │    │ product_type     │               │
│ end_date         │    │ unit             │               │
│ status           │    │ shelf_life_days  │               │
└────────┬─────────┘    └──────┬───────────┘               │
         │ 1:N                 │ 1:N                       │
         │              ┌──────▼─────────┐                 │
         └─────────────►│  log_batches   │◄────────────────┘
                        │────────────────│
                        │ batch_id       │
                        │ plan_id        │           ┌──────────────────┐
                        │ product_id     │           │   inventories    │
                        │ quantity       │1─────────1│──────────────────│
                        │ production_date│           │ inventory_id     │
                        │ expiry_date    │           │ product_id       │
                        │ status         │           │ batch_id         │
                        │ type           │           │ quantity         │
                        └────────┬───────┘           │ expiry_date      │
                                 │ 1:N               └──────────────────┘
                                 ▼
                        ┌─────────────────────────┐
                        │ inventory_transactions  │
                        │─────────────────────────│
                        │ transaction_id          │
                        │ product_id              │
                        │ batch_id                │
                        │ receipt_id              │──► receipts
                        │ type (IMPORT/EXPORT)    │
                        │ quantity                │
                        │ note                    │
                        └─────────────────────────┘
```

### Bảng dữ liệu chính

| Bảng | Mô tả | Số cột |
|------|--------|--------|
| `users` | Người dùng hệ thống | 5 |
| `roles` | Vai trò (Manager, Warehouse Keeper, Store Owner, Shipper) | 2 |
| `stores` | Cửa hàng chi nhánh | 4 |
| `products` | Sản phẩm (nguyên liệu, bán thành phẩm, thành phẩm) | 5 |
| `recipes` | Công thức sản xuất | 4 |
| `recipe_details` | Chi tiết công thức (BOM) | 3 |
| `orders` | Đơn đặt hàng | 6 |
| `order_details` | Chi tiết đơn hàng | 3 |
| `order_detail_fill` | Phân bổ lô hàng cho đơn (FEFO result) | 4 |
| `production_plans` | Kế hoạch sản xuất | 5 |
| `production_plan_details` | Chi tiết kế hoạch sản xuất | 4 |
| `log_batches` | Lô hàng (sản xuất/nhập mua) | 8 |
| `inventories` | Tồn kho theo lô | 4 |
| `inventory_transactions` | Lịch sử nhập/xuất kho | 6 |
| `deliveries` | Chuyến giao hàng | 3 |
| `receipts` | Phiếu xuất kho | 5 |
| `quality_feedbacks` | Phản hồi chất lượng | 4 |
| `reports` | Báo cáo | 3 |

---

## 📡 API Documentation

### Swagger UI

Khi server chạy, truy cập Swagger UI tại:
```
http://localhost:8080/swagger-ui/index.html
```

### Tổng Quan API Endpoints

#### 🔐 Authentication (`/auth`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `POST` | `/auth/login` | Đăng nhập (trả về User) |
| `POST` | `/auth/v2/login` | Đăng nhập v2 (trả về JWT Token) |
| `POST` | `/auth/introspect` | Kiểm tra token hợp lệ |

#### 📦 Orders (`/orders`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/orders` | Lấy tất cả đơn hàng |
| `GET` | `/orders/get-by-store/{storeId}` | Lấy đơn theo cửa hàng |
| `GET` | `/orders/filter-by-status?status=` | Lọc đơn theo trạng thái |
| `GET` | `/orders/get-by-shipper/{shipperId}` | Lấy đơn theo shipper |
| `POST` | `/orders` | Tạo đơn hàng mới |
| `PATCH` | `/orders/update-status/{storeId}` | Cập nhật trạng thái đơn |
| `PATCH` | `/orders/{orderId}/complete` | Hoàn thành đơn hàng |

#### 🚚 Deliveries (`/deliveries`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/deliveries` | Lấy tất cả chuyến giao |
| `GET` | `/deliveries/get-by-shipper/{shipperId}` | Lấy chuyến theo shipper |
| `POST` | `/deliveries/create` | Tạo chuyến giao + gán shipper |
| `PATCH` | `/deliveries/{deliveryId}/start` | Bắt đầu giao hàng |

#### 🧾 Receipts (`/receipts`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/receipts/order/{orderId}` | Lấy phiếu xuất theo đơn hàng |
| `POST` | `/receipts/order/{orderId}` | Tạo phiếu xuất nháp |
| `POST` | `/receipts/confirm` | Xác nhận xuất kho (trừ kho) |

#### 📦 Products (`/products`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/products` | Lấy tất cả sản phẩm |
| `GET` | `/products/get-by-type/{productType}` | Lọc theo loại |
| `POST` | `/products` | Tạo sản phẩm |
| `PUT` | `/products/{productId}` | Cập nhật sản phẩm |

#### 🏪 Inventory (`/inventories`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/inventories` | Lấy tất cả tồn kho |
| `GET` | `/inventories/get-by-id/{inventoryId}` | Lấy tồn kho theo ID |

#### 📋 Log Batches (`/log-batches`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/log-batches` | Lấy tất cả lô hàng |
| `GET` | `/log-batches/{batchId}` | Lấy lô theo ID |
| `GET` | `/log-batches/plan/{planId}` | Lấy lô theo kế hoạch |
| `GET` | `/log-batches/product/{productId}` | Lấy lô theo sản phẩm |
| `GET` | `/log-batches/status/{status}` | Lọc theo trạng thái |
| `POST` | `/log-batches/production` | Tạo lô sản xuất |
| `POST` | `/log-batches/purchase` | Tạo lô nhập mua |
| `PATCH` | `/log-batches/{batchId}/status` | Cập nhật trạng thái lô |
| `PUT` | `/log-batches/{batchId}/expire` | Xử lý hủy lô hết hạn |

#### 🏭 Production Plans (`/production-plans`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/production-plans` | Lấy tất cả kế hoạch |
| `GET` | `/production-plans/{id}` | Lấy kế hoạch theo ID |
| `POST` | `/production-plans` | Tạo kế hoạch sản xuất |

#### 🍳 Recipes (`/recipes`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/recipes` | Lấy tất cả công thức |
| `GET` | `/recipes/search/{keyword}` | Tìm kiếm công thức |

#### 👤 Users (`/users`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/users` | Lấy tất cả người dùng |
| `GET` | `/users/{userId}` | Lấy theo ID |
| `GET` | `/users/shippers` | Lấy tất cả shipper |
| `POST` | `/users` | Tạo người dùng |
| `PUT` | `/users/{userId}` | Cập nhật |
| `DELETE` | `/users/{userId}` | Xóa |

#### 🏪 Stores (`/stores`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/stores` | Lấy tất cả cửa hàng |
| `GET` | `/stores/{id}` | Lấy theo ID |
| `POST` | `/stores` | Tạo cửa hàng |
| `PUT` | `/stores/{id}` | Cập nhật |
| `DELETE` | `/stores/{id}` | Xóa |

#### ⭐ Quality Feedbacks (`/feedbacks`)
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/feedbacks` | Lấy tất cả phản hồi |
| `POST` | `/feedbacks` | Tạo phản hồi mới |

---

## 🔄 Business Flow

### Luồng Đặt Hàng và Giao Hàng

```
 ┌─────────────┐     ┌────────────┐     ┌────────────┐     ┌────────────┐     ┌──────────┐
 │  WAITTING   │────►│ PROCESSING │────►│ DISPATCHED │────►│ DELIVERING │────►│   DONE   │
 │             │     │            │     │            │     │            │     │          │
 │ Store tạo   │     │ Gán shipper│     │ Thủ kho    │     │ Shipper    │     │ Shipper  │
 │ đơn hàng    │     │ + FEFO     │     │ xác nhận   │     │ bắt đầu   │     │ hoàn     │
 │             │     │ allocation │     │ xuất kho   │     │ giao hàng  │     │ thành    │
 └─────────────┘     └────────────┘     └────────────┘     └────────────┘     └──────────┘
```

### Luồng Chi Tiết

```
Bước 1: Store Owner tạo đơn hàng
        └─► Order (status = WAITTING)

Bước 2: Manager gán shipper cho đơn hàng
        ├─► Tạo Delivery + gán Shipper
        ├─► Order (status = PROCESSING)
        └─► Tự động chạy FEFO → tạo OrderDetailFill (giữ chỗ hàng)

Bước 3.1: Thủ kho tạo phiếu xuất (Draft Receipt)
        └─► Receipt (status = DRAFT)

Bước 3.2: Thủ kho xác nhận xuất kho
        ├─► Receipt (status = COMPLETED)
        ├─► Order (status = DISPATCHED)
        ├─► Trừ inventory theo OrderDetailFill
        └─► Ghi InventoryTransaction (EXPORT)

Bước 4: Shipper bắt đầu giao hàng
        └─► Order (status = DELIVERING)

Bước 5: Shipper xác nhận đã giao
        └─► Order (status = DONE)

Bước 6 (Tùy chọn): Store Owner gửi phản hồi chất lượng
        └─► QualityFeedback (rating + comment)
```

### Luồng Xử Lý Hàng Hết Hạn

```
Scheduled Job (00:01 hàng ngày)
    │
    ├─► Tìm LogBatch có expiry_date < today VÀ status = DONE
    │
    ├─► Cập nhật status = WAITING_TO_CANCLE
    │
    └─► Thủ kho xác nhận hủy (PUT /log-batches/{id}/expire)
        ├─► LogBatch status = DAMAGED
        ├─► Inventory quantity = 0
        ├─► Ghi InventoryTransaction (EXPORT - hủy)
        └─► Tạo Report (type = WASTE)
```

---

## 🚀 Cài Đặt & Chạy

### Yêu Cầu

- **Java** 17+
- **Maven** 3.8+
- **PostgreSQL** 12+ (hoặc SQL Server)
- **Git**

### Bước 1: Clone Repository

```bash
git clone https://github.com/<your-username>/kitchen-control-backend.git
cd kitchen-control-backend
```

### Bước 2: Cấu Hình Database

Mở file `src/main/resources/application.properties` và cấu hình database:

```properties
# PostgreSQL Local
spring.datasource.url=jdbc:postgresql://localhost:5432/KitchenControlDBv2?stringtype=unspecified
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### Bước 3: Chạy Ứng Dụng

```bash
# Cách 1: Maven
mvn spring-boot:run

# Cách 2: Build rồi chạy
mvn clean package -DskipTests
java -jar target/KitchenControlBEv1-0.0.1-SNAPSHOT.jar
```

### Bước 4: Kiểm Tra

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

---

## ⚙ Cấu Hình Môi Trường

### Biến cấu hình quan trọng

| Biến | Mô tả | Giá trị mặc định |
|------|--------|-------------------|
| `server.port` | Port chạy server | `8080` |
| `spring.datasource.url` | JDBC URL kết nối database | — |
| `spring.datasource.username` | Username database | — |
| `spring.datasource.password` | Password database | — |
| `spring.jpa.hibernate.ddl-auto` | Chiến lược tạo/cập nhật schema | `update` |
| `jwt.signerKey` | Secret key cho JWT (HS512) | — |

### CORS Configuration

Các origin được phép truy cập:
- `https://swp-web-six.vercel.app` — Frontend trên Vercel
- `http://localhost:3000` — Frontend local development

---

## 🐳 Deployment

### Docker

```bash
# Build Docker image
docker build -t kitchen-control-backend .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/dbname \
  -e SPRING_DATASOURCE_USERNAME=user \
  -e SPRING_DATASOURCE_PASSWORD=pass \
  kitchen-control-backend
```

### Dockerfile (Multi-stage Build)

```dockerfile
# Build Stage
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Render.com

Dự án hiện đang deploy trên **Render.com** với PostgreSQL managed database.

---

## 🤝 Đóng Góp

1. Fork dự án
2. Tạo feature branch (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi (`git commit -m 'Add amazing feature'`)
4. Push lên branch (`git push origin feature/amazing-feature`)
5. Tạo Pull Request

---

## 📄 Tài Liệu Bổ Sung

| Tài liệu | Đường dẫn |
|-----------|-----------|
| Hướng dẫn tạo Entity | [`docs/ENTITY_CREATION_GUIDE.md`](docs/ENTITY_CREATION_GUIDE.md) |
| Hướng dẫn JWT & Spring Security | [`docs/JWT_SPRING_SECURITY_GUIDE.md`](docs/JWT_SPRING_SECURITY_GUIDE.md) |
| Hướng dẫn tính năng Login | [`docs/LOGIN_FEATURE_GUIDE.md`](docs/LOGIN_FEATURE_GUIDE.md) |
| Hướng dẫn WebSocket Real-time | [`docs/WEBSOCKET_REALTIME_GUIDE.md`](docs/WEBSOCKET_REALTIME_GUIDE.md) |

---

