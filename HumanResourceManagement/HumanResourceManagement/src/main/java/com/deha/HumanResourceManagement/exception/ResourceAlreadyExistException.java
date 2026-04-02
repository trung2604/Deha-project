package com.deha.HumanResourceManagement.exception;

public class ResourceAlreadyExistException extends ConflictException {
    public ResourceAlreadyExistException(String message) {
        super(message);
    }
}
