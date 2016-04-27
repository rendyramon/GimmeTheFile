package com.drivfe.gimmethefile.errors;

public class BaseException extends Exception {

    protected String defaultErrorMessage;
    protected String defaultErrorMessageMore;
    protected int statusCode;

    public BaseException(String message) {
        this(message, null, -1);
    }

    public BaseException(String message, int status_code) {
        this(message, null, status_code);
    }

    public BaseException(String message, String longMessage) {
        this(message, longMessage, -1);
    }

    public BaseException(String message, String longMessage, int status_code) {
        defaultErrorMessage = message;
        defaultErrorMessageMore = longMessage;
        statusCode = status_code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getDefaultErrorMessageMore() {
        return defaultErrorMessageMore;
    }

    public String getDefaultErrorMessage() {
        return defaultErrorMessage;
    }
}
