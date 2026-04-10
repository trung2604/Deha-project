package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtSessionResponse;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtSessionStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.impl.OtSessionService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtSessionServiceCheckoutValidationTest {

    @Test
    void checkOut_shouldRejectWhenWorkedDurationIsBelowOfficeMinimum() {
        Office office = new Office();
        office.setId(UUID.randomUUID());
        office.setOtMinHours(1);

        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.EMPLOYEE);
        actor.setOffice(office);

        OtSession session = new OtSession();
        session.setId(UUID.randomUUID());
        session.setUser(actor);
        session.setOffice(office);
        session.setLogDate(LocalDate.now());
        session.setCheckInTime(LocalDateTime.now().minusMinutes(25));
        session.setStatus(OtSessionStatus.CHECKED_IN);

        OtSessionRepository otSessionRepository = mock(OtSessionRepository.class);
        when(otSessionRepository.findByUserAndLogDate(actor, LocalDate.now())).thenReturn(Optional.of(session));

        IAttendanceService attendanceService = mock(IAttendanceService.class);
        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        OtSessionService service = new OtSessionService(
                otSessionRepository,
                mock(OtRequestRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                attendanceService
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.checkOut(List.of("127.0.0.1"))
        );

        assertTrue(exception.getMessage().contains("Minimum OT time is 1 hour(s)"));
        verify(otSessionRepository, never()).save(any(OtSession.class));
    }

    @Test
    void checkOut_shouldSucceedWhenWorkedDurationMeetsOfficeMinimum() {
        Office office = new Office();
        office.setId(UUID.randomUUID());
        office.setOtMinHours(1);

        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.EMPLOYEE);
        actor.setOffice(office);

        OtSession session = new OtSession();
        session.setId(UUID.randomUUID());
        session.setUser(actor);
        session.setOffice(office);
        session.setLogDate(LocalDate.now());
        session.setCheckInTime(LocalDateTime.now().minusHours(1).minusMinutes(2));
        session.setStatus(OtSessionStatus.CHECKED_IN);

        OtSessionRepository otSessionRepository = mock(OtSessionRepository.class);
        when(otSessionRepository.findByUserAndLogDate(actor, LocalDate.now())).thenReturn(Optional.of(session));
        when(otSessionRepository.save(any(OtSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IAttendanceService attendanceService = mock(IAttendanceService.class);
        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        OtSessionService service = new OtSessionService(
                otSessionRepository,
                mock(OtRequestRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                attendanceService
        );

        OtSessionResponse response = service.checkOut(List.of("127.0.0.1"));

        assertEquals(OtSessionStatus.CHECKED_OUT, response.getStatus());
        assertNotNull(response.getCheckOutTime());
        assertEquals(1, response.getMinimumOtHours());
        verify(otSessionRepository, times(1)).save(any(OtSession.class));
    }
}

