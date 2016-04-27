package com.drivfe.gimmethefile.errors;

public class ExtractorNotSupportedException extends BaseException {

//    public String default_error_message = "Extractor not supported";
//    public String default_error_message_more = "Not all websites are supported by youtube-dl";

    public ExtractorNotSupportedException(String message) {
        super(message);
    }

    public ExtractorNotSupportedException(String message, int status_code) {
        super(message, status_code);
    }

    public ExtractorNotSupportedException(String message, String longMessage) {
        super(message, longMessage);
    }

    public ExtractorNotSupportedException(String message, String longMessage, int status_code) {
        super(message, longMessage, status_code);
    }
}
