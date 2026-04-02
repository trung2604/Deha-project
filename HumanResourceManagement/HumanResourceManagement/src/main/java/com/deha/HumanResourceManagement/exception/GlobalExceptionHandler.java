package com.deha.HumanResourceManagement.exception;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<?> handleOptimisticLocking(Exception ex) {
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.CONFLICT.value());
        res.setMessage("Data was modified by another user. Please refresh and retry.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex) {
        log.error("Unexpected server error", ex);
        ApiResponse res = new ApiResponse();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("Unexpected server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}