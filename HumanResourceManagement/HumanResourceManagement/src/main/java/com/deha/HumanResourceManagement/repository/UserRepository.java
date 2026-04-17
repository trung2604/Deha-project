package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByDepartment_Id(UUID departmentId);

    long countByPosition_Id(UUID positionId);
    long countByOffice_Id(UUID officeId);
    List<User> findByOffice_Id(UUID officeId);
    boolean existsByIdAndOffice_Id(UUID userId, UUID officeId);

    @Query("select u.id from User u where u.office.id = :officeId")
    List<UUID> findIdsByOfficeId(@Param("officeId") UUID officeId);

    List<User> findByDepartment(Department department);

    @Query("select u from User u where u.isActive = true")
    List<User> findAllActive();

}
