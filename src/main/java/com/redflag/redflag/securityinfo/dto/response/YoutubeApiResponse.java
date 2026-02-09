package com.redflag.redflag.securityinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoutubeApiResponse(
        List<Item> items
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String id,
            Snippet snippet,
            ContentDetails contentDetails
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(
            String title,
            String channelId,
            Thumbnails thumbnails,
            ResourceId resourceId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnails(
            Thumbnail medium,
            Thumbnail high
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(
            String url
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceId(
            String videoId
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetails(
            String videoId
    ) {}
}