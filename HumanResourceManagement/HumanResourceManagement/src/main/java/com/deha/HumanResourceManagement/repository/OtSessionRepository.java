package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface OtSessionRepository extends JpaRepository<OtSession, UUID> {
    Optional<OtSession> findByUserAndLogDate(User user, LocalDate logDate);
}
