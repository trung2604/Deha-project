package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "positions")
@Data
public class Position {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @OneToMany(mappedBy = "position")
    private List<EmployeePosition> employeePositions;
}
