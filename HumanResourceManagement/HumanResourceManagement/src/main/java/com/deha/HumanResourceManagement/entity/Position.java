package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "positions")
@Data
@EqualsAndHashCode(callSuper = false)
public class Position extends AuditableByUser {

    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private Long version;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "position")
    private List<User> users;

    public void rename(String name) {
        this.name = name;
    }

    public void assignDepartment(Department department) {
        this.department = department;
    }

    public boolean belongsToDepartment(UUID departmentId) {
        return this.department != null && this.department.getId() != null && this.department.getId().equals(departmentId);
    }

    public boolean isNameChanged(String newName) {
        return newName != null && !newName.equalsIgnoreCase(this.name);
    }

    public UUID getOfficeId() {
        return this.department != null && this.department.getOffice() != null
                ? this.department.getOffice().getId()
                : null;
    }
}