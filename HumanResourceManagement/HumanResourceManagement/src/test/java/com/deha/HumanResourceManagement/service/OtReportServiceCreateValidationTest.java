package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.impl.OtReportService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtReportWorkflowService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class OtReportServiceCreateValidationTest {

	@Test
	void create_shouldRejectWhenEligibleHoursBelowOfficeMinimum() {
		Fixture fixture = buildFixture(LocalDateTime.now().minusHours(1).minusMinutes(50), LocalDateTime.now());

		OtReportCreateRequest request = new OtReportCreateRequest();
		request.setOtSessionId(fixture.otSession.getId());
		request.setReportNote("Completed deployment and test evidence");

		BadRequestException exception = assertThrows(BadRequestException.class, () -> fixture.service.create(request));
		assertTrue(exception.getMessage().contains("below office minimum OT hours"));
	}

	@Test
	void create_shouldAutoSetReportedHoursFromEligibleSessionDuration() {
		Fixture fixture = buildFixture();

		OtReportCreateRequest request = new OtReportCreateRequest();
		request.setOtSessionId(fixture.otSession.getId());
		request.setReportNote("Completed deployment and test evidence");

		OtReportResponse response = fixture.service.create(request);
		assertEquals(3, response.getReportedOtHours());
	}

	@Test
	void create_shouldRejectWhenReportNoteTooShortAfterTrim() {
		Fixture fixture = buildFixture();

		OtReportCreateRequest request = new OtReportCreateRequest();
		request.setOtSessionId(fixture.otSession.getId());
		request.setReportNote("   short   ");

		BadRequestException exception = assertThrows(BadRequestException.class, () -> fixture.service.create(request));
		assertTrue(exception.getMessage().contains("at least 10 characters"));
	}

	private Fixture buildFixture() {
		return buildFixture(LocalDateTime.now().minusHours(3).minusMinutes(10), LocalDateTime.now());
	}

	private Fixture buildFixture(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
		Office office = new Office();
		office.setId(UUID.randomUUID());
		office.setOtMinHours(2);

		User actor = new User();
		actor.setId(UUID.randomUUID());
		actor.setRole(Role.ROLE_EMPLOYEE);
		actor.setOffice(office);

		LocalDate logDate = LocalDate.now();

		OtSession otSession = new OtSession();
		otSession.setId(UUID.randomUUID());
		otSession.setUser(actor);
		otSession.setOffice(office);
		otSession.setLogDate(logDate);
		otSession.setCheckInTime(checkInTime);
		otSession.setCheckOutTime(checkOutTime);

		AttendanceLog attendanceLog = new AttendanceLog();
		attendanceLog.setId(UUID.randomUUID());
		attendanceLog.setUser(actor);
		attendanceLog.setOffice(office);
		attendanceLog.setLogDate(logDate);

		OtRequest approvedRequest = new OtRequest();
		approvedRequest.setId(UUID.randomUUID());
		approvedRequest.setStatus(OtRequestStatus.APPROVED);

		OtReportRepository otReportRepository = mock(OtReportRepository.class);
		when(otReportRepository.findByOtSession_Id(otSession.getId())).thenReturn(Optional.empty());
		when(otReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		OtRequestRepository otRequestRepository = mock(OtRequestRepository.class);
		when(otRequestRepository.findByUserAndLogDateAndStatus(actor, logDate, OtRequestStatus.APPROVED))
				.thenReturn(Optional.of(approvedRequest));

		AttendanceLogRepository attendanceLogRepository = mock(AttendanceLogRepository.class);
		when(attendanceLogRepository.findByUserAndLogDate(actor, logDate)).thenReturn(Optional.of(attendanceLog));

		OtSessionRepository otSessionRepository = mock(OtSessionRepository.class);
		when(otSessionRepository.findById(otSession.getId())).thenReturn(Optional.of(otSession));

		AccessScopeService accessScopeService = new AccessScopeService(null) {
			@Override
			public User currentUserOrThrow() {
				return actor;
			}
		};

		OtReportService service = new OtReportService(
				otReportRepository,
				otRequestRepository,
				attendanceLogRepository,
				otSessionRepository,
				accessScopeService,
				new OfficePolicyService(),
				new OtReportWorkflowService()
		);

		Fixture fixture = new Fixture();
		fixture.service = service;
		fixture.otSession = otSession;
		return fixture;
	}

	private static class Fixture {
		private OtReportService service;
		private OtSession otSession;
	}
}

