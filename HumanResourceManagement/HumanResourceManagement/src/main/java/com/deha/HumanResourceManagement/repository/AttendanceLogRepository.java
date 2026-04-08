package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, UUID> {
    boolean existsByUser_Id(UUID userId);

    boolean existsByUserAndLogDate(User user, LocalDate logDate);

    Optional<AttendanceLog> findByUserAndLogDate(User user, LocalDate logDate);

    Optional<AttendanceLog> findByUserAndLogDateAndCheckOutTimeIsNull(
            User user, LocalDate logDate
    );

    List<AttendanceLog> findByUserAndLogDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<AttendanceLog> findByLogDateAndUser_Department_Id(LocalDate logDate, UUID departmentId);

    List<AttendanceLog> findByLogDateAndCheckOutTimeIsNull(LocalDate logDate);
}
