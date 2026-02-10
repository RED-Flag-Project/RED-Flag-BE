package com.redflag.redflag.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // ExampleCase의 caseContent에서 user의 키워드와 유사한 키워드 추출
    public String extractSimilarKeywords(
            String caseContent,
            String userKeywords,
            List<String> detectedSentences) {

        // Gemini 2.5 Flash
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 탐지 원문을 컨텍스트로 추가
        String contextInfo = "";
        if (detectedSentences != null && !detectedSentences.isEmpty()) {
            contextInfo = "\n\n참고 - 사용자 채팅에서 탐지된 원문:\n" +
                    String.join("\n", detectedSentences);
        }

        String prompt = String.format(
                "### 과거 피싱 사례:\n%s\n\n" +
                        "### 현재 사용자 분석에서 탐지된 키워드:\n%s%s\n\n" +
                        "### 작업:\n" +
                        "과거 피싱 사례 텍스트에서 현재 사용자의 키워드와 의미적으로 유사하거나 관련된 " +
                        "핵심 단어/문구를 찾아주세요.\n\n" +
                        "### 규칙:\n" +
                        "1. 2-4개의 키워드만 추출\n" +
                        "2. 쉼표로 구분\n" +
                        "3. 키워드만 반환 (설명 없이)\n" +
                        "4. 피싱/스캠과 관련된 핵심 단어 우선\n" +
                        "5. 유사한 의미의 단어도 포함 (예: 사이트→웹사이트, 포인트→리워드)\n\n" +
                        "추출된 키워드:",
                caseContent,
                userKeywords,
                contextInfo
        );

        // Gemini API 요청 형식
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 100,
                        "topP", 0.8,
                        "topK", 10
                )
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();

            // Gemini 응답 파싱
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            log.info("Gemini 2.5 키워드 추출 완료: {}", text.trim());
            return text.trim();

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage(), e);
            // fallback: caseContent 앞 30자
            return caseContent.substring(0, Math.min(30, caseContent.length()));
        }
    }
}