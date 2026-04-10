package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    Optional<ChatRoom> findByType(ChatRoomType type);  // lấy GENERAL room

    Optional<ChatRoom> findByTypeAndOffice_Id(ChatRoomType type, UUID officeId);

    Optional<ChatRoom> findByTypeAndDepartment_Id(ChatRoomType type, UUID departmentId);

    @Query("""
        SELECT r FROM ChatRoom r
        WHERE (r.type = 'GENERAL' AND r.office.id = :officeId)
           OR (:departmentId IS NOT NULL AND r.type = 'DEPARTMENT' AND r.department.id = :departmentId)
        ORDER BY r.name
        """)
    List<ChatRoom> findRoomsForUser(@Param("officeId") UUID officeId, @Param("departmentId") UUID departmentId);
}
