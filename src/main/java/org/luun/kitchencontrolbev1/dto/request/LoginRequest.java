package org.luun.kitchencontrolbev1.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LoginRequest {
    String username;
    String password;
}
