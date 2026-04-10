package com.deha.HumanResourceManagement.dto.chat;

import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChatRoomResponse {
    private UUID id;
    private ChatRoomType type;
    private String name;
    private OfficeSummary office;
    private DepartmentSummary department;

    public static ChatRoomResponse fromEntity(ChatRoom room) {
        if (room == null) return null;

        Office office = room.getOffice();
        Department department = room.getDepartment();

        OfficeSummary officeSummary = office == null
                ? null
                : new OfficeSummary(office.getId(), office.getName());

        DepartmentSummary departmentSummary = department == null
                ? null
                : new DepartmentSummary(department.getId(), department.getName());

        return new ChatRoomResponse(
                room.getId(),
                room.getType(),
                room.getName(),
                officeSummary,
                departmentSummary
        );
    }

    @Getter
    @AllArgsConstructor
    public static class OfficeSummary {
        private UUID id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class DepartmentSummary {
        private UUID id;
        private String name;
    }
}

