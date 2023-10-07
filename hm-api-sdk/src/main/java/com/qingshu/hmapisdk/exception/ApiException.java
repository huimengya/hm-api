package com.qingshu.hmapisdk.exception;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
public class ApiException extends Exception {

    private static final long serialVersionUID = -7249271723353365668L;
    private int code;
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
