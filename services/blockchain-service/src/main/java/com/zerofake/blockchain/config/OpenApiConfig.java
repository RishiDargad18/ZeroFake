package com.zerofake.blockchain.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blockchainServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ZeroFake Blockchain Service API")
                        .version("v1")
                        .description("REST APIs for Hyperledger Fabric integration, product registration, ownership transfer, verification, and blockchain history."));
    }

}