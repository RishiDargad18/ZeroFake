package com.zerofake.fraud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fraudDetectionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZeroFake Fraud Detection Service API")
                        .description("REST APIs for counterfeit detection, verification, fraud reports, and scan history.")
                        .version("v1"));
    }

}