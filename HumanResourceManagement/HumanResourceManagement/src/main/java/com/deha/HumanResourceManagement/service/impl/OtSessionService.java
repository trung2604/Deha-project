package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.ot.OtSessionResponse;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.entity.enums.OtSessionStatus;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.service.IOtSessionService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OtSessionService implements IOtSessionService {
    private static final String MANUAL_SOURCE = "MANUAL";

    private final OtSessionRepository otSessionRepository;
    private final OtRequestRepository otRequestRepository;
    private final AccessScopeService accessScopeService;
    private final OfficePolicyService officePolicyService;
    private final IAttendanceService attendanceService;

    public OtSessionService(
            OtSessionRepository otSessionRepository,
            OtRequestRepository otRequestRepository,
            AccessScopeService accessScopeService,
            OfficePolicyService officePolicyService,
            IAttendanceService attendanceService
    ) {
        this.otSessionRepository = otSessionRepository;
        this.otRequestRepository = otRequestRepository;
        this.accessScopeService = accessScopeService;
        this.officePolicyService = officePolicyService;
        this.attendanceService = attendanceService;
    }

    @Override
    @Transactional
    public OtSessionResponse checkIn(List<String> clientIps) {
        User actor = currentOtActorOrThrow("check in OT");
        if (actor.getOffice() == null) {
            throw new BadRequestException("User is not assigned to any office");
        }

        attendanceService.validateOfficeIpAccess(actor.getOffice(), clientIps);
        LocalDate today = LocalDate.now();
        LocalTime regularDayLatestCheckout = officePolicyService.latestCheckoutTime(actor.getOffice());
        if (otSessionRepository.findByUserAndLogDate(actor, today).isPresent()) {
            throw new BadRequestException("OT check-in already exists for today");
        }
        if (LocalTime.now().isBefore(regularDayLatestCheckout)) {
            throw new BadRequestException("OT check-in is only allowed after latest checkout time");
        }

        OtRequest approvedRequest = otRequestRepository.findByUserAndLogDateAndStatus(actor, today, OtRequestStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException("Approved OT request is required before OT check-in"));

        OtSession session = new OtSession();
        session.setUser(actor);
        session.setOffice(actor.getOffice());
        session.setOtRequest(approvedRequest);
        session.setLogDate(today);
        session.setCheckInTime(LocalDateTime.now());
        session.setSource(MANUAL_SOURCE);
        session.setStatus(OtSessionStatus.CHECKED_IN);
        otSessionRepository.save(session);
        return OtSessionResponse.fromEntity(session);
    }

    @Override
    @Transactional
    public OtSessionResponse checkOut(List<String> clientIps) {
        User actor = currentOtActorOrThrow("check out OT");
        LocalDate today = LocalDate.now();
        OtSession session = otSessionRepository.findByUserAndLogDate(actor, today)
                .orElseThrow(() -> new BadRequestException("No OT check-in record found for today"));

        attendanceService.validateOfficeIpAccess(session.getOffice(), clientIps);
        if (session.getCheckOutTime() != null) {
            throw new BadRequestException("OT session already checked out");
        }
        LocalDateTime effectiveCheckout = LocalDateTime.now();
        if (!effectiveCheckout.isAfter(session.getCheckInTime())) {
            throw new BadRequestException("Invalid OT checkout time by office OT policy");
        }
        session.setCheckOutTime(effectiveCheckout);
        session.setStatus(OtSessionStatus.CHECKED_OUT);
        otSessionRepository.save(session);
        return OtSessionResponse.fromEntity(session);
    }

    @Override
    @Transactional(readOnly = true)
    public OtSessionResponse today() {
        User actor = currentOtActorOrThrow("view personal OT session");
        return otSessionRepository.findByUserAndLogDate(actor, LocalDate.now())
                .map(OtSessionResponse::fromEntity)
                .orElse(null);
    }

    private User currentOtActorOrThrow(String action) {
        User actor = accessScopeService.currentUserOrThrow();
        boolean canSelfManageOt = accessScopeService.isEmployee(actor)
                || accessScopeService.isDepartmentManager(actor);
        if (!canSelfManageOt) {
            throw new ForbiddenException("You do not have permission to " + action);
        }
        return actor;
    }
}

