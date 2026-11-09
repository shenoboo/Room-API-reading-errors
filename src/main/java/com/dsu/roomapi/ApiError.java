package com.dsu.roomapi;

import java.util.List;

import org.springframework.http.HttpStatus;

/** One body for every failure. fieldErrors is empty unless @Valid failed. */
public record ApiError(int status, String error, String message, String path,
                       List<FieldMessage> fieldErrors) {

    public record FieldMessage(String field, String message) { }

    public static ApiError of(HttpStatus status, String message, String path,
                              List<FieldMessage> fieldErrors) {
        return new ApiError(status.value(), status.getReasonPhrase(),
                message, path, fieldErrors);
    }
}
