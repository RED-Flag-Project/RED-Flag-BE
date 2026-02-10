package com.redflag.redflag.global.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI redflagOpenAPI() {
        String cookieName = "user_id";

        // 1. SecurityScheme 정의 (쿠키 기반)
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY) // 쿠키는 APIKEY 타입으로 취급
                .in(SecurityScheme.In.COOKIE)
                .name(cookieName);

        // 2. 전체 API에 적용될 보안 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("CookieAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("RED FLAG API")
                        .description("RED FLAG 보안 분석 API\n\n" +
                                "## 🍪 사용 방법\n" +
                                "1. `/api/user/issue`에서 ID 발급\n" +
                                "2. 우측 상단 **Authorize** 버튼 클릭\n" +
                                "3. `Value`란에 발급받은 UUID 입력")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes("CookieAuth", cookieAuth))
                .addSecurityItem(securityRequirement);
    }
    /*
    @Bean
    public OpenAPI redflagOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RED FLAG API")
                        .description("RED FLAG 보안 분석 API\n\n" +
                                "## 🍪 쿠키 사용 안내\n" +
                                "1. `/api/user/issue`를 먼저 호출하여 사용자 ID를 발급받으세요.\n" +
                                "2. 응답의 'userId' 값을 복사하세요.\n" +
                                "3. 다른 API 호출 시 아래 'Cookie' 파라미터에 `user_id=복사한UUID` 형식으로 입력하세요.\n\n" +
                                "**예시:** `user_id=550e8400-e29b-41d4-a716-446655440000`")
                        .version("v1.0.0"));
    }
     */

    /**
     * 모든 API에 Cookie 파라미터 추가
     * Swagger UI에서 수동으로 쿠키를 입력할 수 있게 함
     */
    /*
    @Bean
    public OperationCustomizer addCookieParameter() {
        return (operation, handlerMethod) -> {
            // /api/user/issue는 쿠키가 선택사항이므로 required=false
            boolean isUserIssue = handlerMethod.getMethod().getName().equals("issueUserId");
            
            Parameter cookieParam = new Parameter()
                    .in("cookie")
                    .name("user_id")
                    .description(isUserIssue ? 
                            "사용자 UUID (선택사항 - 없으면 새로 생성)" : 
                            "사용자 UUID (필수 - /api/user/issue에서 발급)")
                    .required(!isUserIssue)
                    .schema(new StringSchema()
                            .example("550e8400-e29b-41d4-a716-446655440000"));
            
            operation.addParametersItem(cookieParam);
            return operation;
        };
    }

     */
}

