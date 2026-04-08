package com.deha.HumanResourceManagement.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyOtpResponse {

    private final String resetToken;

}