package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "office_wifi_ips",
        uniqueConstraints = @UniqueConstraint(columnNames = {"office_id", "ip_wifi"})
)
@Data
@EqualsAndHashCode(callSuper = false)
public class OfficeWifiIp extends AuditableByUser {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "ip_wifi", nullable = false, length = 64)
    private String ipWifi;

    public void applyDetails(Office office, String ipWifi) {
        this.office = office;
        this.ipWifi = ipWifi;
    }
}

