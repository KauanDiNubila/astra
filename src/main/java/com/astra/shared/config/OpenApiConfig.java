package com.astra.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI astraOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Astra API")
                .description("API do Astra - registro de tempo focado (estudos e trabalho).")
                .version("v1"));
    }
}
