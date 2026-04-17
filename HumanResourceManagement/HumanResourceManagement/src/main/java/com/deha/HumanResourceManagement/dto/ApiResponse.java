package com.deha.HumanResourceManagement.dto;

import lombok.Data;

@Data
public class ApiResponse {
    private String message;
    private Integer status;
    private Object data;
}