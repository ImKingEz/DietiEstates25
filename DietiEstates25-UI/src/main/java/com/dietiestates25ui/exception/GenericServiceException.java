package com.dietiestates25ui.exception;

public class GenericServiceException extends Exception {
    public GenericServiceException(String s, Exception e) {
        super(s, e);
    }
    public GenericServiceException(String s) {
        super(s);
    }
}
