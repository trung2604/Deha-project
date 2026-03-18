package com.deha.HumanResourceManagement.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class ApiResponse {
    private String message;
    private Integer status;
    private Object data;
}
