package org.luun.kitchencontrolbev1.enums;

public enum ErrorCode {

    INVALID_LOGIN(500, "Invalid username or password")
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
