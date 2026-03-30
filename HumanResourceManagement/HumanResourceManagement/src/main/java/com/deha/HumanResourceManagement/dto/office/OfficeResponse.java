package com.deha.HumanResourceManagement.dto.office;

import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeResponse {
    private UUID id;
    private String name;
    private String description;
    private List<String> ipWifiIps;
    private Integer baseWorkHoursPerDay;
    private Double otWeekdayMultiplier;
    private Double otWeekendMultiplier;
    private Double otHolidayMultiplier;
    private Double otNightBonusMultiplier;
    private Integer otMinHours;
    private LocalTime latestCheckoutTime;

    public static OfficeResponse fromEntity(Office office) {
        if (office == null) return null;
        return new OfficeResponse(
                office.getId(),
                office.getName(),
                office.getDescription(),
                office.getWifiIps() == null
                        ? List.of()
                        : office.getWifiIps().stream()
                        .map(OfficeWifiIp::getIpWifi)
                        .toList(),
                office.getStandardWorkHours(),
                office.getOtWeekdayMultiplier(),
                office.getOtWeekendMultiplier(),
                office.getOtHolidayMultiplier(),
                office.getOtNightBonusMultiplier(),
                office.getOtMinHours(),
                office.getLatestCheckoutTime()
        );
    }
}
