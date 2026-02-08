package com.redflag.redflag.dashboard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AgeDistributionApiResponse(
        Integer currentCount,
        List<AgeData> data,
        Integer matchCount,
        Integer page,
        Integer perPage,
        Integer totalCount
) {
    public record AgeData(
            @JsonProperty("20대이하")
            Integer under20,

            @JsonProperty("30대")
            Integer thirties,

            @JsonProperty("40대")
            Integer forties,

            @JsonProperty("50대")
            Integer fifties,

            @JsonProperty("60대")
            Integer sixties,

            @JsonProperty("70대이상")
            Integer over70,

            @JsonProperty("구분")
            Integer year
    ) {}
}
