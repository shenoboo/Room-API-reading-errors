package com.dsu.roomapi;

/** "No such thing" (maps to 404 in ApiExceptionHandler). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
