package com.dsu.roomapi;

/** We know who you are, and the answer is no (maps to 403). Thrown by a service (rule C). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
