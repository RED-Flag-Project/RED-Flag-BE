package com.redflag.redflag.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GenderDistributionApiResponse(
        Integer currentCount,
        List<GenderData> data,
        Integer matchCount,
        Integer page,
        Integer perPage,
        Integer totalCount
) {
    public record GenderData(
            @JsonProperty("구분")
            Integer year,

            @JsonProperty("남성")
            Integer male,

            @JsonProperty("여성")
            Integer female
    ) {}
}
