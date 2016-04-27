package com.drivfe.gimmethefile.errors;

public class HTTPErrorException extends BaseException {

//    public String ERROR = "There was an HTTP error";

    public HTTPErrorException(String message) {
        super(message);
    }

    public HTTPErrorException(String message, int status_code) {
        super(message, status_code);
    }

    public HTTPErrorException(String message, String longMessage) {
        super(message, longMessage);
    }

    public HTTPErrorException(String message, String longMessage, int status_code) {
        super(message, longMessage, status_code);
    }
}
