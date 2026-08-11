package com.zerofake.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI productServiceOpenApi() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("ZeroFake Product Service API")
                                .version("1.0.0")
                                .description(
                                        "Product catalogue, categories, batches and QR code "
                                                + "generation for the ZeroFake anti-counterfeiting platform."
                                )
                                .contact(new Contact().name("ZeroFake Team"))
                )
                .schemaRequirement(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );
    }
}
