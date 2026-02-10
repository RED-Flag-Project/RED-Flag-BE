package com.redflag.redflag.global.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI redflagOpenAPI() {
        String cookieName = "user_id";

        // 1. 배포 서버 주소 설정 (HTTPS 강제)
        Server prodServer = new Server();
        prodServer.setUrl("https://cdxfgv2my3.ap-northeast-1.awsapprunner.com");
        prodServer.setDescription("운영 서버 (HTTPS)");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("로컬 서버 (HTTP)");

        // 2. SecurityScheme 정의 (쿠키 기반)
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name(cookieName);

        // 3. 보안 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("CookieAuth");

        return new OpenAPI()
                .servers(List.of(prodServer, localServer)) // 서버 설정 적용
                .info(new Info()
                        .title("RED FLAG API")
                        .description("RED FLAG 보안 분석 API\n\n" +
                                "## 🍪 사용 방법\n" +
                                "1. `/api/v1/user/issue`에서 ID 발급 (POST)\n" +
                                "2. 우측 상단 **Authorize** 버튼 클릭\n" +
                                "3. `Value`란에 `user_id=발급받은UUID` 입력")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes("CookieAuth", cookieAuth))
                .addSecurityItem(securityRequirement);
    }
}

