package com.cdac.hpuat.issuetopatient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {
    private String msg;
    private Integer status; // 1 for success, 0 for failure
    private T data;

    public static <T> ApiResponse<T> success(String msg, T data) {
        return new ApiResponse<>(msg, 1, data);
    }

    public static <T> ApiResponse<T> error(String msg, T data) {
        return new ApiResponse<>(msg, 0, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("Success", 1, data);
    }
}
