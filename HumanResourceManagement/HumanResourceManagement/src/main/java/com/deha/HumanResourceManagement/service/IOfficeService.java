package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.office.OfficePolicyRequest;
import com.deha.HumanResourceManagement.dto.office.OfficePolicyResponse;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeResponse;
import com.deha.HumanResourceManagement.entity.Office;

import java.util.List;
import java.util.UUID;

public interface IOfficeService {
    List<OfficeResponse> getAll();

    Office findById(UUID id);

    OfficeResponse create(OfficeRequest request);

    OfficeResponse update(UUID id, OfficeRequest request);

    void delete(UUID id);

    OfficePolicyResponse getMyPolicy();

    OfficePolicyResponse updateMyPolicy(OfficePolicyRequest request);
}

