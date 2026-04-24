package com.deha.HumanResourceManagement.exception;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mail.MailException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.validation.BindException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex){

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Validation failed");
        res.setData(errors);
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler({BindException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<?> handleBinding(Exception ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Invalid request parameters");
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleNotReadable(HttpMessageNotReadableException ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Malformed JSON request");
        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(ex.getStatus().value());
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(res);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, "Account is inactive");
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLocked(LockedException ex) {
        return build(HttpStatus.FORBIDDEN, "Account is locked");
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<?> handleInsufficientAuth(InsufficientAuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication is required");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwt(JwtException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<?> handleOptimisticLocking(Exception ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.CONFLICT.value());
        res.setMessage("Data was modified by another user. Please refresh and retry.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.BAD_REQUEST.value());
        res.setMessage("Avatar file must be <= 2MB");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNotFound(NoResourceFoundException ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.NOT_FOUND.value());
        res.setMessage("Resource not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<?> handleNotWritable(HttpMessageNotWritableException ex) {
        log.error("JSON serialization error", ex);
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("Response serialization error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler({MailException.class, IllegalStateException.class})
    public ResponseEntity<?> handleMailFailure(Exception ex) {
        log.error("Email delivery error", ex);
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("Email delivery failed. Please check mail configuration and SMTP credentials.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        log.error("Unexpected server error", ex);
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("Unexpected server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    private ResponseEntity<ApiResponse> build(HttpStatus status, String message) {
        ApiResponse res = new ApiResponse();
        res.setStatus(status.value());
        res.setMessage(message);
        return ResponseEntity.status(status).body(res);
    }
}