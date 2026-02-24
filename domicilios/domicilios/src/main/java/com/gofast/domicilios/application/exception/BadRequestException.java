package com.gofast.domicilios.application.exception;

public class BadRequestException extends RuntimeException {

    private final String code;
    private final String field;

    public BadRequestException(String message) {
        super(message);
        this.code = null;
        this.field = null;
    }

    public BadRequestException(String message, String code) {
        super(message);
        this.code = code;
        this.field = null;
    }

    public BadRequestException(String message, String code, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public String getCode() {
        return code;
    }

    public String getField() {
        return field;
    }
}

