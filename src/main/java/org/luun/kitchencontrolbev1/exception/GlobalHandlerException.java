package org.luun.kitchencontrolbev1.exception;

import org.luun.kitchencontrolbev1.dto.response.ApiResponse;
import org.luun.kitchencontrolbev1.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.INVALID_LOGIN.getCode());
        apiResponse.setResult(ErrorCode.INVALID_LOGIN.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
