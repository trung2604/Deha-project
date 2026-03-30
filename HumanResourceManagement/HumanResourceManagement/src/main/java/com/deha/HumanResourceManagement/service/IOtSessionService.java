package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtSessionResponse;

import java.util.List;

public interface IOtSessionService {
    OtSessionResponse checkIn(List<String> clientIps);

    OtSessionResponse checkOut(List<String> clientIps);

    OtSessionResponse today();
}

