package com.deha.HumanResourceManagement.mapper.ot;

import com.deha.HumanResourceManagement.entity.User;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OtMapperSupport {

    @Named("userId")
    public UUID userId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("userName")
    public String userName(User user) {
        if (user == null) {
            return null;
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }
}

