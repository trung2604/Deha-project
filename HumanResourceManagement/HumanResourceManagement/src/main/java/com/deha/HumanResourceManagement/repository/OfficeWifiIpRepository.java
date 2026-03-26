package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfficeWifiIpRepository extends JpaRepository<OfficeWifiIp, UUID> {
    List<OfficeWifiIp> findByOffice(Office office);
}
