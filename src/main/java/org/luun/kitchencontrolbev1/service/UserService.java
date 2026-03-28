package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.UserRequest;
import org.luun.kitchencontrolbev1.dto.response.UserResponse;
import org.luun.kitchencontrolbev1.entity.User;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserResponseById(Integer userId);
    User getUserById(Integer userId);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Integer userId, UserRequest request);
    void deleteUser(Integer userId);
    List<UserResponse> getAllShippers();
}
