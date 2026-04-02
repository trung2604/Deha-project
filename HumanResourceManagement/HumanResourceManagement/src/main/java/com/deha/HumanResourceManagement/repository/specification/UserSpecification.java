package com.deha.HumanResourceManagement.repository.specification;

import com.deha.HumanResourceManagement.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecification {

    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("firstName").as(String.class)), pattern),
                    cb.like(cb.lower(root.get("lastName").as(String.class)), pattern),
                    cb.like(cb.lower(root.get("email").as(String.class)), pattern),
                    cb.like(cb.lower(root.get("phone").as(String.class)), pattern)
            );
        };
    }

    public static Specification<User> hasOffice(UUID officeId) {
        return (root, query, cb) ->
                officeId == null ? null :
                        cb.equal(root.get("office").get("id"), officeId);
    }

    public static Specification<User> hasDepartment(UUID departmentId) {
        return (root, query, cb) ->
                departmentId == null ? null :
                        cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<User> hasPosition(UUID positionId) {
        return (root, query, cb) ->
                positionId == null ? null :
                        cb.equal(root.get("position").get("id"), positionId);
    }

    public static Specification<User> isActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null :
                        cb.equal(root.get("isActive"), active);
    }
}
