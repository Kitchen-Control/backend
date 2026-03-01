# Feature Specification: Get All Shippers

Tài liệu này hướng dẫn chi tiết cách implement chức năng lấy danh sách tất cả người dùng có vai trò là **SHIPPER**.

## 1. Mục tiêu
*   Lấy danh sách các `User` mà `Role` của họ có `roleName` là `SHIPPER`.
*   Tối ưu hóa truy vấn để tránh N+1 query (fetch user rồi mới fetch role).

## 2. Implementation Steps

### Step 1: Repository Layer (`UserRepository.java`)

Sử dụng JPQL hoặc Spring Data JPA naming convention để join bảng `User` và `Role`.

**Cách 1: Dùng Naming Convention (Đơn giản nhất)**
Spring Data JPA tự động hiểu `Role` là thuộc tính trong `User`, và `RoleName` là thuộc tính trong `Role`.

```java
// File: org/luun/kitchencontrolbev1/repository/UserRepository.java

import org.luun.kitchencontrolbev1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    // Tìm User dựa trên thuộc tính roleName của object Role
    List<User> findByRoleRoleName(role_name roleName);
}
```

**Cách 2: Dùng @Query (Tối ưu hơn nếu muốn fetch Eager)**
Nếu bạn muốn đảm bảo chỉ 1 câu query duy nhất được bắn xuống DB (JOIN fetch), hãy dùng cách này.

```java
// File: org/luun/kitchencontrolbev1/repository/UserRepository.java

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u JOIN FETCH u.role r WHERE r.roleName = :roleName")
    List<User> findAllByRoleName(@Param("roleName") role_name roleName);
}
```

### Step 2: Service Layer

#### Interface (`UserService.java`)

```java
// File: org/luun/kitchencontrolbev1/service/UserService.java

import org.luun.kitchencontrolbev1.dto.response.UserResponse; // Giả sử bạn có DTO này
import java.util.List;

public interface UserService {
    List<UserResponse> getAllShippers();
}
```

#### Implementation (`UserServiceImpl.java`)

```java
// File: org/luun/kitchencontrolbev1/service/impl/UserServiceImpl.java

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllShippers() {
        // Gọi repository với enum SHIPPER
        List<User> shippers = userRepository.findByRoleRoleName(role_name.SHIPPER);

        // Map entity sang DTO
        return shippers.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        // Logic map dữ liệu
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                // ... các trường khác
                .build();
    }
}
```

### Step 3: Controller Layer (`UserController.java`)

```java
// File: org/luun/kitchencontrolbev1/controller/UserController.java

@GetMapping("/shippers")
public ResponseEntity<List<UserResponse>> getAllShippers() {
    return ResponseEntity.ok(userService.getAllShippers());
}
```

---

## 3. Lưu ý quan trọng
1.  **Enum Value**: Đảm bảo enum `role_name.SHIPPER` khớp với giá trị trong database.
2.  **Performance**: Nếu danh sách shipper lớn, cân nhắc dùng `Pageable` để phân trang.
3.  **DTO**: Luôn trả về DTO (`UserResponse`) thay vì Entity (`User`) để tránh lộ thông tin nhạy cảm như `password`.
