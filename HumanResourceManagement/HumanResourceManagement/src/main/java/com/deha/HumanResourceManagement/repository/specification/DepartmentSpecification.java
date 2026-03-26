package com.deha.HumanResourceManagement.repository.specification;

import com.deha.HumanResourceManagement.entity.Department;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class DepartmentSpecification {
    public static Specification<Department> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;

            String pattern = "%" + keyword + "%";

            return cb.or(
                    cb.like(root.get("name"), pattern),
                    cb.and(
                            cb.isNotNull(root.get("description")),
                            cb.like(root.get("description"), pattern)
                    )
            );
        };
    }

    public static Specification<Department> hasOffice(UUID officeId) {
        return (root, query, cb) ->
                officeId == null ? null :
                        cb.equal(root.get("office").get("id"), officeId);
    }
}
