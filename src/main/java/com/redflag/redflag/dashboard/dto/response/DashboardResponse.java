package com.redflag.redflag.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드 응답 데이터")
public record DashboardResponse(
        @Schema(description = "오늘 탐지된 건수")
        TodayDetection todayDetection,

        @Schema(description = "2025년 총 피해 규모")
        TotalDamageStats totalDamageStats,

        @Schema(description = "연령대별 피해 비율 (단위: %)")
        AgeDistribution ageDistribution,

        @Schema(description = "성별 피해 비율 (단위: %)")
        GenderDistribution genderDistribution
) {
    @Schema(description = "오늘 탐지된 건수 (riskScore >= 50)")
    public record TodayDetection(
            @Schema(description = "오늘 탐지된 건수", example = "12")
            Long count
    ) {}

    @Schema(description = "2025년 총 피해 규모 통계")
    public record TotalDamageStats(
            @Schema(description = "누적 피해액 (원)", example = "1133000000000")
            Long totalDamageAmount,

            @Schema(description = "전년 대비 피해액 증감률 (%)", example = "56.1")
            Double yearOverYearChangeRate,

            @Schema(description = "총 발생 건수 (건)", example = "18676")
            Long totalIncidentCount,

            @Schema(description = "전년 대비 발생 건수 증감률 (%)", example = "15.6")
            Double incidentChangeRate
    ) {}

    @Schema(description = "연령대별 피해 비율")
    public record AgeDistribution(
            @Schema(description = "20대 이하 비율 (%)", example = "25.3")
            Double under20,

            @Schema(description = "30대 비율 (%)", example = "18.5")
            Double thirties,

            @Schema(description = "40대 비율 (%)", example = "15.2")
            Double forties,

            @Schema(description = "50대 비율 (%)", example = "20.8")
            Double fifties,

            @Schema(description = "60대 비율 (%)", example = "18.0")
            Double sixties,

            @Schema(description = "70대 이상 비율 (%)", example = "2.2")
            Double over70
    ) {}

    @Schema(description = "성별 피해 비율")
    public record GenderDistribution(
            @Schema(description = "남성 비율 (%)", example = "52.1")
            Double male,

            @Schema(description = "여성 비율 (%)", example = "47.9")
            Double female
    ) {}
}
