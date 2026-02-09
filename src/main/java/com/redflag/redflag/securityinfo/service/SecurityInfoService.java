package com.redflag.redflag.securityinfo.service;

import com.redflag.redflag.global.exception.GeneralException;
import com.redflag.redflag.global.exception.code.status.ErrorStatus;
import com.redflag.redflag.securityinfo.dto.response.SecurityInfoResponse;
import com.redflag.redflag.securityinfo.dto.response.SecurityNewsListResponse;
import com.redflag.redflag.securityinfo.dto.response.YoutubeApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityInfoService {

    @Value("${api.youtube.key}")
    private String youtubeApiKey;

    @Value("${api.youtube.playlist-id}")
    private String playlistId;

    @Value("${api.google-rss.url}")
    private String rssUrl;

    private final RestTemplate restTemplate;

    private static final int NEWS_LIMIT = 2;
    private static final int NEWS_LIST_LIMIT = 10;
    private static final int VIDEO_LIMIT = 5;

    /**
     * 보안 정보 조회 (메인 대시보드용 2건)
     */
    public SecurityInfoResponse getSecurityInfo() {
        List<SecurityInfoResponse.SecurityNews> securityNews = fetchNewsFromRss(NEWS_LIMIT).stream()
                .map(news -> new SecurityInfoResponse.SecurityNews(
                        news.id(), news.source(), news.title(), news.summary(), news.publishedAt(), news.linkUrl()))
                .toList();

        SecurityInfoResponse.YoutubeInfo youtubeInfo = getYoutubeInfo();
        return new SecurityInfoResponse(securityNews, youtubeInfo);
    }

    /**
     * 보안 뉴스 목록 조회 (목록 페이지용 10건)
     */
    public SecurityNewsListResponse getSecurityNewsList() {
        List<SecurityNewsListResponse.SecurityNews> newsList = fetchNewsFromRss(NEWS_LIST_LIMIT);
        return new SecurityNewsListResponse(newsList);
    }

    /**
     * 유튜브 플레이리스트에서 영상 조회
     */
    private SecurityInfoResponse.YoutubeInfo getYoutubeInfo() {
        try {
            String url = String.format(
                    "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet,contentDetails&playlistId=%s&maxResults=%d&key=%s",
                    playlistId, VIDEO_LIMIT, youtubeApiKey
            );

            YoutubeApiResponse response = restTemplate.getForObject(url, YoutubeApiResponse.class);

            if (response == null || response.items() == null || response.items().isEmpty()) {
                throw new GeneralException(ErrorStatus.SECURITY_YOUTUBE_FETCH_ERROR);
            }

            List<SecurityInfoResponse.YoutubeVideo> videos = new ArrayList<>();
            String channelId = null;

            for (int i = 0; i < response.items().size(); i++) {
                YoutubeApiResponse.Item item = response.items().get(i);
                String videoId = item.contentDetails().videoId();
                
                // 첫 번째 아이템에서 채널 ID 추출
                if (i == 0 && item.snippet().channelId() != null) {
                    channelId = item.snippet().channelId();
                }

                videos.add(new SecurityInfoResponse.YoutubeVideo(
                        "yt_" + String.format("%03d", i + 1),
                        item.snippet().title(),
                        item.snippet().thumbnails().high().url(),
                        "https://youtu.be/" + videoId
                ));
            }

            // 채널 URL 생성 (채널 ID 방식)
            String channelUrl = channelId != null 
                    ? "https://www.youtube.com/channel/" + channelId
                    : "https://www.youtube.com/@경찰청";

            return new SecurityInfoResponse.YoutubeInfo(channelUrl, videos);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("유튜브 영상 조회 실패", e);
            throw new GeneralException(ErrorStatus.SECURITY_YOUTUBE_FETCH_ERROR);
        }
    }

    /**
     * XML 태그 값 추출
     */
    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    /**
     * 뉴스 소스 추출 (title에서 - 앞부분)
     */
    private String extractSource(String title) {
        if (title.contains(" - ")) {
            return title.substring(title.lastIndexOf(" - ") + 3);
        }
        return "뉴스";
    }

    /**
     * 타이틀 정리 (소스 제거)
     */
    private String cleanTitle(String title) {
        if (title.contains(" - ")) {
            return title.substring(0, title.lastIndexOf(" - "));
        }
        return title;
    }

    /**
     * 날짜 포맷 변환 (RFC 822 -> yyyy.MM.dd)
     */
    private String formatDate(String pubDate) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
            return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", pubDate);
            return "";
        }
    }

    /**
     * HTML 태그 제거 및 요약문 정리
     */
    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        // HTML 태그 제거
        String text = html.replaceAll("<[^>]*>", "");

        // HTML 엔티티 디코딩
        text = text.replace("&nbsp;", " ")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");

        // 여러 공백을 하나로
        text = text.replaceAll("\\s+", " ").trim();

        // 너무 길면 자르기 (100자)
        if (text.length() > 100) {
            text = text.substring(0, 97) + "...";
        }

        return text;
    }

    /**
     * 공통 로직: RSS 피드에서 뉴스 데이터를 파싱하여 리스트로 반환
     */
    private List<SecurityNewsListResponse.SecurityNews> fetchNewsFromRss(int limit) {
        try (InputStream inputStream = new URL(rssUrl).openStream()) {
            List<SecurityNewsListResponse.SecurityNews> newsList = new ArrayList<>();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList itemList = doc.getElementsByTagName("item");

            for (int i = 0; i < itemList.getLength() && i < limit; i++) {
                Element item = (Element) itemList.item(i);

                String title = getTagValue("title", item);
                String link = getTagValue("link", item);
                String pubDate = getTagValue("pubDate", item);
                String description = getTagValue("description", item);

                newsList.add(new SecurityNewsListResponse.SecurityNews(
                        "news_" + String.format("%03d", i + 1),
                        extractSource(title),
                        cleanTitle(title),
                        cleanHtml(description), // 여기서 summary 처리
                        formatDate(pubDate),
                        link
                ));
            }
            return newsList;
        } catch (Exception e) {
            log.error("RSS 뉴스 파싱 중 에러 발생", e);
            throw new GeneralException(ErrorStatus.SECURITY_NEWS_FETCH_ERROR);
        }
    }
}
