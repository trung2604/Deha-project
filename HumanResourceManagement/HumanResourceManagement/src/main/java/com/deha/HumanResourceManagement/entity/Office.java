package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "offices")
@Data
public class Office {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "name", nullable = false, length = 120, unique = true)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "office", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OfficeWifiIp> wifiIps = new ArrayList<>();

    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "office", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    public void applyDetails(String name, String description) {
        this.name = name;
        this.description = description;
    }
}