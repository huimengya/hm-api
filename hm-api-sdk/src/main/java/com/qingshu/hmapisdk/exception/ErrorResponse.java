package com.qingshu.hmapisdk.exception;

import lombok.Data;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
public class ErrorResponse {
    private String message;
    private int code;
}
