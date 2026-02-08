package com.redflag.redflag.dashboard.service;

import com.redflag.redflag.dashboard.dto.response.AgeDistributionApiResponse;
import com.redflag.redflag.dashboard.dto.response.DashboardResponse;
import com.redflag.redflag.dashboard.dto.response.GenderDistributionApiResponse;
import com.redflag.redflag.global.exception.GeneralException;
import com.redflag.redflag.global.exception.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    @Value("${api.odcloud.service-key}")
    private String serviceKey;

    @Value("${api.odcloud.age-distribution-url}")
    private String ageDistributionUrl;

    @Value("${api.odcloud.gender-distribution-url}")
    private String genderDistributionUrl;

    private final RestTemplate restTemplate;

    /**
     * 대시보드
     */
    public DashboardResponse getDashboardData() {
        // 연령대별 데이터 조회
        DashboardResponse.AgeDistribution ageDistribution = getAgeDistribution();

        // 성별 데이터 조회
        DashboardResponse.GenderDistribution genderDistribution = getGenderDistribution();

        // 총 피해 규모 (1차 목데이터)
        DashboardResponse.TotalDamageStats totalDamageStats = createMockTotalDamageStats();

        return new DashboardResponse(
                totalDamageStats,
                ageDistribution,
                genderDistribution
        );
    }

    private DashboardResponse.AgeDistribution getAgeDistribution() {
        try {
            String url = ageDistributionUrl + "?serviceKey=" + serviceKey;
            AgeDistributionApiResponse response = restTemplate.getForObject(url, AgeDistributionApiResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new GeneralException(ErrorStatus.DASHBOARD_AGE_DATA_NOT_FOUND);
            }

            // 가장 최근 연도 데이터 찾기
            AgeDistributionApiResponse.AgeData latestData = response.data().stream()
                    .max(Comparator.comparing(AgeDistributionApiResponse.AgeData::year))
                    .orElseThrow(() -> new GeneralException(ErrorStatus.DASHBOARD_AGE_LATEST_DATA_NOT_FOUND));

            // 전체 합계 계산
            int total = latestData.under20() + latestData.thirties() + latestData.forties()
                    + latestData.fifties() + latestData.sixties() + latestData.over70();

            // 비율 계산
            return new DashboardResponse.AgeDistribution(
                    calculatePercentage(latestData.under20(), total),
                    calculatePercentage(latestData.thirties(), total),
                    calculatePercentage(latestData.forties(), total),
                    calculatePercentage(latestData.fifties(), total),
                    calculatePercentage(latestData.sixties(), total),
                    calculatePercentage(latestData.over70(), total)
            );

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("연령대별 데이터 조회 실패", e);
            throw new GeneralException(ErrorStatus.DASHBOARD_EXTERNAL_API_ERROR);
        }
    }

    private DashboardResponse.GenderDistribution getGenderDistribution() {
        try {
            String url = genderDistributionUrl + "?serviceKey=" + serviceKey;
            GenderDistributionApiResponse response = restTemplate.getForObject(url, GenderDistributionApiResponse.class);

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new GeneralException(ErrorStatus.DASHBOARD_GENDER_DATA_NOT_FOUND);
            }

            // 가장 최근 연도 데이터 찾기
            GenderDistributionApiResponse.GenderData latestData = response.data().stream()
                    .max(Comparator.comparing(GenderDistributionApiResponse.GenderData::year))
                    .orElseThrow(() -> new GeneralException(ErrorStatus.DASHBOARD_GENDER_LATEST_DATA_NOT_FOUND));

            // 전체 합계 계산
            int total = latestData.male() + latestData.female();

            // 비율 계산
            return new DashboardResponse.GenderDistribution(
                    calculatePercentage(latestData.male(), total),
                    calculatePercentage(latestData.female(), total)
            );

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("성별 데이터 조회 실패", e);
            throw new GeneralException(ErrorStatus.DASHBOARD_EXTERNAL_API_ERROR);
        }
    }

    // 목데이터 생성 (1차)
    private DashboardResponse.TotalDamageStats createMockTotalDamageStats() {
        return new DashboardResponse.TotalDamageStats(
                1_234_567_890_000L, // 1조 2345억
                15.5, // 전년 대비 15.5% 증가
                5_432_100L, // 543만원
                85.3 // 하루 평균 85.3건
        );
    }

    // 퍼센트 비율 계산
    private Double calculatePercentage(Integer value, Integer total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((value * 100.0 / total) * 10.0) / 10.0; // 소수점 첫째자리까지
    }
}
