package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtRequestResponse;

import java.util.List;
import java.util.UUID;

public interface IOtRequestService {
    OtRequestResponse create(OtRequestCreateRequest request);

    OtRequestResponse decide(UUID id, OtDecisionRequest request);

    List<OtRequestResponse> listMy();

    List<OtRequestResponse> listPendingForApproverScope();

    List<OtRequestResponse> listByApprovalScope();
}

