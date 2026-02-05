package com.redflag.redflag.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI redflagOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RED FLAG API")
                        .version("v1.0.0"));

    }
}
