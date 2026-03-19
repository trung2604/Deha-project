package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    boolean existsByName(String name);

    @Query("SELECT p FROM Position p WHERE p.department.id = :departmentId")
    List<Position> findAllByDepartmentId(UUID departmentId);

    @Query("SELECT COUNT(p) > 0 FROM Position p WHERE p.department.id = :departmentId AND LOWER(p.name) = LOWER(:name)")
    boolean existsByNameInDepartment(UUID departmentId, String name);

    void deleteAllByDepartmentId(UUID departmentId);

}
