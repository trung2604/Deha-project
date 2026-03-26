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

    @Column(name = "standard_work_hours", nullable = false)
    private int standardWorkHours = 9;

    @Column(name = "ot_min_hours", nullable = false)
    private int otMinHours = 1;

    @Column(name = "ot_weekday_multiplier", nullable = false)
    private double otWeekdayMultiplier = 1.5d;

    @Column(name = "ot_weekend_multiplier", nullable = false)
    private double otWeekendMultiplier = 2.0d;

    @Column(name = "ot_holiday_multiplier", nullable = false)
    private double otHolidayMultiplier = 3.0d;

    @Column(name = "ot_night_bonus_multiplier", nullable = false)
    private double otNightBonusMultiplier = 0.3d;

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