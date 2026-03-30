package com.deha.HumanResourceManagement.dto.office;

import com.deha.HumanResourceManagement.entity.Office;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficePolicyResponse {
    private UUID officeId;
    private String officeName;
    private Integer baseWorkHoursPerDay;
    private Integer otMinHours;
    private LocalTime latestCheckoutTime;
    private Double otWeekdayMultiplier;
    private Double otWeekendMultiplier;
    private Double otHolidayMultiplier;
    private Double otNightBonusMultiplier;

    public static OfficePolicyResponse fromEntity(Office office) {
        if (office == null) return null;
        return new OfficePolicyResponse(
                office.getId(),
                office.getName(),
                office.getStandardWorkHours(),
                office.getOtMinHours(),
                office.getLatestCheckoutTime(),
                office.getOtWeekdayMultiplier(),
                office.getOtWeekendMultiplier(),
                office.getOtHolidayMultiplier(),
                office.getOtNightBonusMultiplier()
        );
    }
}
