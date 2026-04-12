package com.deha.HumanResourceManagement.dto.chat;

import com.deha.HumanResourceManagement.entity.enums.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private UUID id;
    private ChatRoomType type;
    private String name;
    private OfficeSummary office;
    private DepartmentSummary department;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfficeSummary {
        private UUID id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentSummary {
        private UUID id;
        private String name;
    }
}
