package com.deha.HumanResourceManagement.dto.office;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private Long expectedVersion;
}
