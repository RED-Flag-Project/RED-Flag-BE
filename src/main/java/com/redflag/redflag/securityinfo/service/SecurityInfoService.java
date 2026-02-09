package com.redflag.redflag.securityinfo.service;

import com.redflag.redflag.global.exception.GeneralException;
import com.redflag.redflag.global.exception.code.status.ErrorStatus;
import com.redflag.redflag.securityinfo.dto.response.SecurityInfoResponse;
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
    private static final int VIDEO_LIMIT = 5;

    /**
     * 보안 정보 조회
     */
    public SecurityInfoResponse getSecurityInfo() {
        // 1. 보안 뉴스 조회 (Google RSS)
        List<SecurityInfoResponse.SecurityNews> securityNews = getSecurityNews();

        // 2. 유튜브 영상 조회
        SecurityInfoResponse.YoutubeInfo youtubeInfo = getYoutubeInfo();

        return new SecurityInfoResponse(securityNews, youtubeInfo);
    }

    /**
     * Google RSS에서 보안 뉴스 조회
     */
    private List<SecurityInfoResponse.SecurityNews> getSecurityNews() {
        try {
            List<SecurityInfoResponse.SecurityNews> newsList = new ArrayList<>();

            // RSS 피드 파싱
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            URL url = new URL(rssUrl);
            InputStream inputStream = url.openStream();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // item 태그 추출
            NodeList itemList = doc.getElementsByTagName("item");

            int count = 0;
            for (int i = 0; i < itemList.getLength() && count < NEWS_LIMIT; i++) {
                Element item = (Element) itemList.item(i);

                String title = getTagValue("title", item);
                String link = getTagValue("link", item);
                String pubDate = getTagValue("pubDate", item);
                String description = getTagValue("description", item);

                // 뉴스 소스 추출 (title에서 - 앞부분)
                String source = extractSource(title);

                // 날짜 포맷 변환 (RFC 822 -> yyyy.MM.dd)
                String formattedDate = formatDate(pubDate);

                // 요약문 정리 (HTML 태그 제거)
                String summary = cleanHtml(description);

                newsList.add(new SecurityInfoResponse.SecurityNews(
                        "news_" + String.format("%03d", i + 1),
                        source,
                        cleanTitle(title),
                        summary,
                        formattedDate,
                        link
                ));

                count++;
            }

            inputStream.close();
            return newsList;

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("보안 뉴스 조회 실패", e);
            throw new GeneralException(ErrorStatus.SECURITY_NEWS_FETCH_ERROR);
        }
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
}
