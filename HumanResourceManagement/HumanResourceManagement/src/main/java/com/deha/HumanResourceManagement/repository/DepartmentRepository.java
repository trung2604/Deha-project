package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    boolean existsByName(String name);

    @Query(value = """
            SELECT *
            FROM departments d
            WHERE (
                :keyword IS NULL
                OR d.name ILIKE CONCAT('%', :keyword, '%')
                OR (d.description IS NOT NULL AND d.description ILIKE CONCAT('%', :keyword, '%'))
            )
            ORDER BY d.name ASC
            """,
            nativeQuery = true)
    List<Department> searchDepartments(@Param("keyword") String keyword);
}
