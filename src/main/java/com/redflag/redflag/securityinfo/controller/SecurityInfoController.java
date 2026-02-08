package com.redflag.redflag.securityinfo.controller;

import com.redflag.redflag.dashboard.dto.response.DashboardResponse;
import com.redflag.redflag.global.response.ApiResponse;
import com.redflag.redflag.securityinfo.dto.response.SecurityInfoResponse;
import com.redflag.redflag.securityinfo.service.SecurityInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/security-info")
public class SecurityInfoController {

    private final SecurityInfoService securityInfoService;

    /**
     * 보안 정보 조회 (뉴스 + 유튜브)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SecurityInfoResponse>> getSecurityInfo() {
        SecurityInfoResponse response = securityInfoService.getSecurityInfo();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
