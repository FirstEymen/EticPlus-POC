package com.EticPlus_POC.exception;

public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final String errorDesc;

    public BusinessException(String errorCode, String errorDesc) {
        super(errorDesc);
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }
}
