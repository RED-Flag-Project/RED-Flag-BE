package com.redflag.redflag.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;


@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))  // 연결 타임아웃 10초
                .setReadTimeout(Duration.ofSeconds(30))     // 읽기 타임아웃 30초
                .build();
    }
}
