package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtSessionResponse;

public interface IOtSessionService {
    OtSessionResponse checkIn(String clientIps);

    OtSessionResponse checkOut(String clientIps);

    OtSessionResponse today();
}

