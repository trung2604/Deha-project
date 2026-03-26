package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import org.springframework.http.HttpStatus;

public abstract class ApiControllerSupport {

    protected ApiResponse success(String message, HttpStatus status, Object data) {
        ApiResponse response = new ApiResponse();
        response.setMessage(message);
        response.setStatus(status.value());
        response.setData(data);
        return response;
    }
}

