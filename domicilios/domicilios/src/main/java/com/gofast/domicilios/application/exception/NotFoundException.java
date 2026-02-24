package com.gofast.domicilios.application.exception;

public class NotFoundException extends RuntimeException {

    private final String code;

    public NotFoundException(String message) {
        super(message);
        this.code = null;
    }

    public NotFoundException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}


