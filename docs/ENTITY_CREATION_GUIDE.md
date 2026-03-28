# Entity Creation Guide

This document provides a step-by-step guide to creating JPA entities from the provided DBML database schema for the KitchenControlBEv1 project. Each section corresponds to a table in the database schema and includes the necessary Java code and explanations.

## Prerequisites

Before you start, make sure you have the following dependencies in your `pom.xml`:

-   **Spring Data JPA**: For repository support and entity management.
-   **Lombok**: To reduce boilerplate code (getters, setters, constructors).
-   **Your specific database driver** (e.g., `mysql-connector-java`).

```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Add your database driver here -->
    <!-- e.g., for MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## Recommended Package Structure

To keep the code organized, create the following packages within `src/main/java/org/luun/kitchencontrolbev1`:

-   `entity`: For all your JPA entity classes.
-   `enums`: For all your Java `Enum` types.

## Creating Enums

First, let's create the Java `Enum` types that are used in your entities.

**`ProductType.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum ProductType {
    RAW_MATERIAL,
    SEMI_FINISHED,
    FINISHED_PRODUCT
}
```

**`OrderStatus.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum OrderStatus {
    WAITTING,
    PROCESSING,
    DELIVERING,
    DONE,
    DAMAGED,
    CANCLED
}
```

**`LogBatchStatus.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum LogBatchStatus {
    PROCESSING,
    DONE,
    EXPIRED,
    DAMAGED
}
```

**`LogBatchType.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum LogBatchType {
    PRODUCTION,
    PURCHASE
}
```

**`InventoryTransactionType.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum InventoryTransactionType {
    IMPORT,
    EXPORT
}
```
**`DeliveryStatus.java`**
```java
package org.luun.kitchencontrolbev1.enums;

public enum DeliveryStatus {
    WAITTING,
    PROCESSING,
    DONE
}
```
## Creating Entities (Step-by-Step)

Now, let's create the entity class for each table.

### 1. Role Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Role.java`

```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", length = 255)
    private String roleName;
}
```

### 2. Store Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Store.java`

```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stores")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "store_name", length = 255)
    private String storeName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "phone", length = 255)
    private String phone;
}
```

### 3. User Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/User.java`

```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne
    @JoinColumn(name = "store_id", unique = true)
    private Store store;

    @OneToMany(mappedBy = "shipper")
    private List<Delivery> deliveries;
}
```

### 4. Product Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Product.java`

```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.ProductType;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @Column(name = "unit", length = 255)
    private String unit;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;
}
```

### 5. Recipe & RecipeDetail Entities

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Recipe.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Integer recipeId;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "recipe_name")
    private String recipeName;

    @Column(name = "yield_quantity")
    private Float yieldQuantity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "recipe")
    private List<RecipeDetail> recipeDetails;
}
```

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/RecipeDetail.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recipe_details")
public class RecipeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_detail_id")
    private Integer recipeDetailId;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "raw_material_id")
    private Product rawMaterial;

    @Column(name = "quantity")
    private Float quantity;
}
```

### 6. ProductionPlan & ProductionPlanDetail Entities

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/ProductionPlan.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "production_plans")
public class ProductionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "plan_date")
    private LocalDate planDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", length = 255)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "productionPlan")
    private List<ProductionPlanDetail> productionPlanDetails;
}
```

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/ProductionPlanDetail.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "production_plan_details")
public class ProductionPlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_detail_id")
    private Integer planDetailId;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private ProductionPlan productionPlan;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Float quantity;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
```

### 7. Order & OrderDetail Entities

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Delivery.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer deliveryId;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @ManyToOne
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Order.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @OneToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "img", length = 255)
    private String img;

    @Column(name = "comment", length = 255)
    private String comment;

    @OneToMany(mappedBy = "order")
    private List<OrderDetail> orderDetails;
}
```

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/OrderDetail.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_details")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer orderDetailId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Float quantity;
}
```

### 8. LogBatch and Inventory Entities

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/LogBatch.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.LogBatchStatus;
import org.luun.kitchencontrolbev1.enums.LogBatchType;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "log_batches")
public class LogBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Integer batchId;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private ProductionPlan plan;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity")
    private Float quantity;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LogBatchStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private LogBatchType type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Inventory.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne
    @JoinColumn(name = "batch_id", unique = true)
    private LogBatch batch;

    @Column(name = "quantity")
    private Float quantity;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
```

### 9. InventoryTransaction Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/InventoryTransaction.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.luun.kitchencontrolbev1.enums.InventoryTransactionType;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private LogBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InventoryTransactionType type;

    @Column(name = "quantity")
    private Float quantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
```

### 10. QualityFeedback Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/QualityFeedback.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "quality_feedbacks")
public class QualityFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Integer feedbackId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### 11. Report Entity

**File:** `src/main/java/org/luun/kitchencontrolbev1/entity/Report.java`
```java
package org.luun.kitchencontrolbev1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "report_type", length = 255)
    private String reportType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
```

This guide provides the basic structure for your entities. You can now proceed to create repositories for them and start building your application's business logic.
