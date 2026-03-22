package com.deha.HumanResourceManagement.repository;


import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByDepartment_Id(UUID departmentId);

    long countByPosition_Id(UUID positionId);

    @Query(value = """
    SELECT *
    FROM users u
    WHERE
        (
            :keyword IS NULL
            OR u.first_name ILIKE CONCAT('%', :keyword, '%')
            OR u.last_name ILIKE CONCAT('%', :keyword, '%')
            OR u.email ILIKE CONCAT('%', :keyword, '%')
        )
        AND (:departmentId IS NULL OR u.department_id = :departmentId)
        AND (:positionId IS NULL OR u.position_id = :positionId)
        AND (:active IS NULL OR u.is_active = :active)
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM users u
    WHERE
        (
            :keyword IS NULL
            OR u.first_name ILIKE CONCAT('%', :keyword, '%')
            OR u.last_name ILIKE CONCAT('%', :keyword, '%')
            OR u.email ILIKE CONCAT('%', :keyword, '%')
        )
        AND (:departmentId IS NULL OR u.department_id = :departmentId)
        AND (:positionId IS NULL OR u.position_id = :positionId)
        AND (:active IS NULL OR u.is_active = :active)
    """,
            nativeQuery = true)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("departmentId") UUID departmentId,
            @Param("positionId") UUID positionId,
            @Param("active") Boolean active,
            Pageable pageable
    );

    Page<User> findAll(Pageable pageable);
}
