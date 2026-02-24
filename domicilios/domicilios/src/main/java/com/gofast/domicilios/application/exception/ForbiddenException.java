package com.gofast.domicilios.application.exception;

public class ForbiddenException extends RuntimeException {

    private final String code;

    public ForbiddenException(String message) {
        super(message);
        this.code = null;
    }

    public ForbiddenException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

