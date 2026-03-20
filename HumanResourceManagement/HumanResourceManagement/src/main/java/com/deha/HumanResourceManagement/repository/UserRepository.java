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

    @Query("""
    SELECT u FROM User u
    WHERE 
        u.firstName ILIKE CONCAT('%', :keyword, '%')
        OR u.lastName ILIKE CONCAT('%', :keyword, '%')
        OR u.email ILIKE CONCAT('%', :keyword, '%')
    """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    Page<User> findAll(Pageable pageable);
}
