package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtRequestRepository extends JpaRepository<OtRequest, UUID> {
    boolean existsByUser_Id(UUID userId);

    Optional<OtRequest> findByUserAndLogDate(User user, LocalDate logDate);
    Optional<OtRequest> findByUserAndLogDateAndStatus(User user, LocalDate logDate, OtRequestStatus status);
    List<OtRequest> findByOffice_IdAndLogDate(UUID officeId, LocalDate logDate);
    List<OtRequest> findByUser_IdOrderByLogDateDesc(UUID userId);
    List<OtRequest> findByOffice_IdAndStatusOrderByLogDateDesc(UUID officeId, OtRequestStatus status);
    List<OtRequest> findByUser_Department_IdAndStatusOrderByLogDateDesc(UUID departmentId, OtRequestStatus status);
    List<OtRequest> findByStatusOrderByLogDateDesc(OtRequestStatus status);
    List<OtRequest> findByLogDateAndStatusIn(LocalDate logDate, List<OtRequestStatus> statuses);

    List<OtRequest> findAllByUser_Department_IdOrderByLogDateDesc(UUID departmentId);

    List<OtRequest> findAllByOffice_IdOrderByLogDateDesc(UUID officeId);
}
