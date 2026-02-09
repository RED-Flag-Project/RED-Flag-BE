package com.redflag.redflag.dashboard.controller;

import com.redflag.redflag.dashboard.dto.response.DashboardResponse;
import com.redflag.redflag.dashboard.service.DashboardService;
import com.redflag.redflag.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 데이터 조회", description = "최근 대시보드 데이터를 조회합니다. " +
            "(오늘 탐지된 건수, 총 피해규모, 연령대별 피해 비율)")

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
