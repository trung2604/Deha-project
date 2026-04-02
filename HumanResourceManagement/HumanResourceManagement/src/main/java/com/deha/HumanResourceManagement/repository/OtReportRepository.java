package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtReportRepository extends JpaRepository<OtReport, UUID> {
    Optional<OtReport> findByAttendanceLog_Id(UUID attendanceLogId);
    Optional<OtReport> findByOtSession_Id(UUID otSessionId);

    List<OtReport> findByAttendanceLog_User_IdAndAttendanceLog_LogDateBetweenAndStatus(
            UUID userId,
            LocalDate fromDate,
            LocalDate toDate,
            OtReportStatus status
    );

    List<OtReport> findByAttendanceLog_Office_IdAndStatusOrderByAttendanceLog_LogDateDesc(UUID officeId, OtReportStatus status);

    List<OtReport> findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(UUID departmentId, OtReportStatus status);

    List<OtReport> findByStatusOrderByAttendanceLog_LogDateDesc(OtReportStatus status);

    List<OtReport> findAllByAttendanceLog_User_Department_IdOrderByAttendanceLog_LogDateDesc(UUID departmentId);

    List<OtReport> findAllByAttendanceLog_User_Office_IdOrderByAttendanceLog_LogDateDesc(UUID officeId);

    List<OtReport> findByAttendanceLog_User_IdOrderByAttendanceLog_LogDateDesc(UUID userId);
}

