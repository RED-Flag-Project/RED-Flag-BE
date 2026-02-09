package com.redflag.redflag.securityinfo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "보안 뉴스 목록 응답")
public record SecurityNewsListResponse(
        @Schema(description = "보안 뉴스 목록 (최대 10건)")
        List<SecurityNews> newsList
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
}
