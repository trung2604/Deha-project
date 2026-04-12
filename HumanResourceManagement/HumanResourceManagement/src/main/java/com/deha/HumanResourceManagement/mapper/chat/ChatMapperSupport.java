package com.deha.HumanResourceManagement.mapper.chat;

import com.deha.HumanResourceManagement.dto.chat.ChatRoomResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChatMapperSupport {

    @Named("senderId")
    public UUID senderId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("senderName")
    public String senderName(User user) {
        if (user == null) {
            return null;
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }

    @Named("officeSummary")
    public ChatRoomResponse.OfficeSummary officeSummary(Office office) {
        return office == null ? null : new ChatRoomResponse.OfficeSummary(office.getId(), office.getName());
    }

    @Named("departmentSummary")
    public ChatRoomResponse.DepartmentSummary departmentSummary(Department department) {
        return department == null ? null : new ChatRoomResponse.DepartmentSummary(department.getId(), department.getName());
    }
}

