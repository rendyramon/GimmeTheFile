package com.drivfe.gimmethefile.errors;

public class FailedToConnectException extends BaseException {
//    protected String default_error_message = "The app failed to connect to retrieve the different formats";

    public FailedToConnectException(String message) {
        super(message);
    }

    public FailedToConnectException(String message, int status_code) {
        super(message, status_code);
    }

    public FailedToConnectException(String message, String longMessage) {
        super(message, longMessage);
    }

    public FailedToConnectException(String message, String longMessage, int status_code) {
        super(message, longMessage, status_code);
    }
}
