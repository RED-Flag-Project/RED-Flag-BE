package com.redflag.redflag.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",           // React 개발 서버
                        "http://localhost:5173",           // Vite 개발 서버
                        "https://cdxfgv2my3.ap-northeast-1.awsapprunner.com",
                        "https://red-flag-beta.vercel.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)  // 쿠키 전송 허용
                .maxAge(3600);           // preflight 요청 캐시 시간 (1시간)
    }
}
