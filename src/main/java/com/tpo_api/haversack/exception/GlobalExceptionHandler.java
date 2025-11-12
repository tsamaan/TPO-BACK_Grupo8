package com.tpo_api.haversack.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== Excepciones Personalizadas ==========
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.NOT_FOUND.value(), 
            "Not Found", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(), 
            "Bad Request", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(), 
            "Conflict", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized access: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(), 
            "Unauthorized", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("Invalid credentials attempt - Path: {}", request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(), 
            "Invalid Credentials", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ========== Excepciones de Spring Security ==========
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.FORBIDDEN.value(), 
            "Forbidden", 
            "You don't have permission to access this resource", 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials - Path: {}", request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.UNAUTHORIZED.value(), 
            "Unauthorized", 
            "Invalid username or password", 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ========== Excepciones de Base de Datos ==========
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {} - Path: {}", ex.getMostSpecificCause().getMessage(), request.getRequestURI());
        
        String message = "Data integrity violation";
        String detailedMessage = ex.getMostSpecificCause().getMessage();
        
        // Detectar duplicados
        if (detailedMessage.contains("Duplicate entry") || detailedMessage.contains("unique constraint")) {
            message = "This record already exists";
        }
        
        ApiError error = new ApiError(
            HttpStatus.CONFLICT.value(), 
            "Conflict", 
            message, 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ========== Excepciones de Validación ==========
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(), 
            "Bad Request", 
            ex.getMessage(), 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: {} - Path: {}", ex.getName(), request.getRequestURI());
        
        String message = String.format(
            "Invalid value for parameter '%s'. Expected type: %s", 
            ex.getName(), 
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(), 
            "Bad Request", 
            message, 
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Override
    @SuppressWarnings("null")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        log.warn("Validation failed - Path: {}", request.getDescription(false));
        
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(), 
            "Validation Failed", 
            "One or more fields have validation errors", 
            request.getDescription(false)
        );
        
        for (var fieldErrorObj : ex.getBindingResult().getFieldErrors()) {
            FieldError fe = (FieldError) fieldErrorObj;
            error.addError(fe.getField(), fe.getDefaultMessage());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @Override
    @SuppressWarnings("null")
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        
        log.warn("Missing parameter: {} - Path: {}", ex.getParameterName(), request.getDescription(false));
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ApiError error = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            message,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ========== Excepciones de Runtime ==========

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage() != null ? ex.getMessage() : "A runtime error occurred",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ========== Excepción Genérica (última red de seguridad) ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);

        // TEMPORAL: Mostrar detalles del error en desarrollo para debugging
        String detailedMessage = "An unexpected error occurred. Please try again later.";
        String exceptionType = ex.getClass().getSimpleName();

        // Incluir causa raíz si existe
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        // Mensaje más detallado para debugging
        detailedMessage = String.format("[%s] %s | Root cause: %s",
            exceptionType,
            ex.getMessage() != null ? ex.getMessage() : "No message",
            rootCause.getMessage() != null ? rootCause.getMessage() : "No root cause"
        );

        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            detailedMessage,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
