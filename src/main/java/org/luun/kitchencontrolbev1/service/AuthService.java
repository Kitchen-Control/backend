package org.luun.kitchencontrolbev1.service;

import org.luun.kitchencontrolbev1.dto.request.LoginRequest;
import org.luun.kitchencontrolbev1.entity.User;

public interface AuthService {
    User login(LoginRequest loginRequest);
}
