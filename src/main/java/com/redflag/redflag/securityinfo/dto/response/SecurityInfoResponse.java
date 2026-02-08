package com.redflag.redflag.securityinfo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보안 정보 응답 데이터")
public record SecurityInfoResponse(
        @Schema(description = "보안 뉴스 목록 (최대 2건)")
        List<SecurityNews> securityNews,

        @Schema(description = "유튜브 영상 정보")
        YoutubeInfo youtube
) {
    @Schema(description = "보안 뉴스 정보")
    public record SecurityNews(
            @Schema(description = "뉴스 ID", example = "news_001")
            String id,

            @Schema(description = "뉴스 출처", example = "경찰청 보도자료")
            String source,

            @Schema(description = "뉴스 제목", example = "신종 보이스피싱 '자녀 납치' 수법 주의보")
            String title,

            @Schema(description = "뉴스 요약", example = "최근 AI 목소리 변조 기술을 악용한 자녀 납치 빙자 보이스피싱이 기승을 부리고 있어...")
            String summary,

            @Schema(description = "발행일", example = "2023.10.24")
            String publishedAt,

            @Schema(description = "뉴스 링크 URL", example = "https://news.example.com/article/1")
            String linkUrl
    ) {}

    @Schema(description = "유튜브 정보")
    public record YoutubeInfo(
            @Schema(description = "유튜브 채널 URL", example = "https://www.youtube.com/@polinlove")
            String channelUrl,

            @Schema(description = "유튜브 영상 목록 (최대 2건)")
            List<YoutubeVideo> videos
    ) {}

    @Schema(description = "유튜브 영상 정보")
    public record YoutubeVideo(
            @Schema(description = "영상 ID", example = "yt_001")
            String id,

            @Schema(description = "영상 제목", example = "[경찰청] 보이스피싱, 아는 만큼 예방합니다")
            String title,

            @Schema(description = "썸네일 이미지 URL", example = "https://cdn.redflag.com/thumbs/video1.jpg")
            String thumbnailUrl,

            @Schema(description = "영상 URL", example = "https://youtu.be/example1")
            String videoUrl
    ) {}
}
