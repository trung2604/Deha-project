package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfficeRepository extends JpaRepository<Office, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
