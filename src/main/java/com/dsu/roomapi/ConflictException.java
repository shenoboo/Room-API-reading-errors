package com.dsu.roomapi;

/** A valid request that clashes with data that already exists (maps to 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
