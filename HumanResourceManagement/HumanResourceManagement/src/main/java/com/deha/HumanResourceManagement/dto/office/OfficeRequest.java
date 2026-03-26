package com.deha.HumanResourceManagement.dto.office;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OfficeRequest {
    @NotBlank(message = "Office name is required")
    private String name;
    private String description;

    @NotNull(message = "WiFi IP list is required")
    @Size(min = 1, message = "Office must have at least 1 WiFi IP")
    private List<String> ipWifiIps;

    @NotNull(message = "Base work hours per day is required")
    @Positive(message = "Base work hours per day must be greater than 0")
    private Integer baseWorkHoursPerDay;

    @NotNull(message = "OT weekday multiplier is required")
    @Positive(message = "OT weekday multiplier must be greater than 0")
    private Double otWeekdayMultiplier;

    @NotNull(message = "OT weekend multiplier is required")
    @Positive(message = "OT weekend multiplier must be greater than 0")
    private Double otWeekendMultiplier;

    @NotNull(message = "OT holiday multiplier is required")
    @Positive(message = "OT holiday multiplier must be greater than 0")
    private Double otHolidayMultiplier;

    @NotNull(message = "OT night bonus multiplier is required")
    @PositiveOrZero(message = "OT night bonus multiplier must be greater than or equal to 0")
    private Double otNightBonusMultiplier;
}
