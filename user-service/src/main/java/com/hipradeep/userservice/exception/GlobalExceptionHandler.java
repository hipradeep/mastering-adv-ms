package com.hipradeep.userservice.exception;

import com.hipradeep.common.dto.ApiResponse;
import com.hipradeep.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        // Using common lib ApiResponse for error handling
        ApiResponse<?> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        ApiResponse<?> response = ApiResponse.error("Internal server error", "INTERNAL_ERROR");
        return ResponseEntity.internalServerError().body(response);
    }
}