package com.globallearning.courseservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 * Swagger UI is available at /api/v1/swagger-ui.html in dev environments.
 * It is disabled via application-prod.yml in production.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI globalizedCourseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Globalized Course Preview & Progress API")
                        .description("Production-grade locale-aware course preview and progress tracking service " +
                                "for the Udemy Globalization Platform team.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Globalization Platform Team")
                                .email("globalization@learning.internal"))
                        .license(new License()
                                .name("Internal — Not for external distribution")));
    }
}
