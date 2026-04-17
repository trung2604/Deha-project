package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IOtReportService {
    OtReportResponse create(OtReportCreateRequest request);

    OtReportResponse create(OtReportCreateRequest request, MultipartFile evidenceFile);

    OtReportResponse decide(UUID id, OtDecisionRequest request);

    List<OtReportResponse> listByApprovalScope();

    List<OtReportResponse> listPendingForScope();

    List<OtReportResponse> listMy();
}

