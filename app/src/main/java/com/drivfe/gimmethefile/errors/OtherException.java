package com.drivfe.gimmethefile.errors;

public class OtherException extends BaseException {
//    protected String default_error_message = "An error has occured";

    public OtherException(String message) {
        super(message);
    }

    public OtherException(String message, int status_code) {
        super(message, status_code);
    }

    public OtherException(String message, String longMessage) {
        super(message, longMessage);
    }

    public OtherException(String message, String longMessage, int status_code) {
        super(message, longMessage, status_code);
    }
}
