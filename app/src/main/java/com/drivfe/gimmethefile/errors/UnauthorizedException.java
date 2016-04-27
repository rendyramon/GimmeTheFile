package com.drivfe.gimmethefile.errors;

public class UnauthorizedException extends BaseException {

//    public String default_error_message = "Forbidden access";
//    public String default_error_message_more = "Some websites, like Youtube, have videos " +
//            "that are restricted to a certain country, age group or only logged in accounts. " +
//            "This app does not currently support any methods to bypass these restrictions.";

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, int status_code) {
        super(message, status_code);
    }

    public UnauthorizedException(String message, String longMessage) {
        super(message, longMessage);
    }

    public UnauthorizedException(String message, String longMessage, int status_code) {
        super(message, longMessage, status_code);
    }
}
