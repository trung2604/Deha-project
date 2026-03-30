package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.service.IOtSessionService;
import com.deha.HumanResourceManagement.service.support.ClientIpResolverService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ot-sessions")
public class OtSessionController extends ApiControllerSupport {
    private final IOtSessionService otSessionService;
    private final ClientIpResolverService clientIpResolverService;

    public OtSessionController(IOtSessionService otSessionService, ClientIpResolverService clientIpResolverService) {
        this.otSessionService = otSessionService;
        this.clientIpResolverService = clientIpResolverService;
    }

    @PostMapping("/check-in")
    public ApiResponse checkIn(HttpServletRequest request) {
        return success(
                "OT checked in successfully",
                HttpStatus.OK,
                otSessionService.checkIn(clientIpResolverService.extractClientIps(request))
        );
    }

    @PostMapping("/check-out")
    public ApiResponse checkOut(HttpServletRequest request) {
        return success(
                "OT checked out successfully",
                HttpStatus.OK,
                otSessionService.checkOut(clientIpResolverService.extractClientIps(request))
        );
    }

    @GetMapping("/today")
    public ApiResponse today() {
        return success("OT session retrieved successfully", HttpStatus.OK, otSessionService.today());
    }
}
