package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    boolean existsByName(String name);
}
