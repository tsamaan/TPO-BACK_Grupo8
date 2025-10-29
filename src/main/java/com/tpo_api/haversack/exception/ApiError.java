package com.tpo_api.haversack.exception;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard API error payload returned by the global exception handler.
 */
public class ApiError {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldViolation> errors;

    public ApiError() {
        this.timestamp = OffsetDateTime.now();
        this.errors = new ArrayList<>();
    }

    public ApiError(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public List<FieldViolation> getErrors() { return errors; }

    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public void setStatus(int status) { this.status = status; }
    public void setError(String error) { this.error = error; }
    public void setMessage(String message) { this.message = message; }
    public void setPath(String path) { this.path = path; }
    public void setErrors(List<FieldViolation> errors) { this.errors = errors; }

    public void addError(String field, String message) {
        if (this.errors == null) this.errors = new ArrayList<>();
        this.errors.add(new FieldViolation(field, message));
    }

    public static class FieldViolation {
        private String field;
        private String message;

        public FieldViolation() {}
        public FieldViolation(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
        public void setField(String field) { this.field = field; }
        public void setMessage(String message) { this.message = message; }
    }
}
