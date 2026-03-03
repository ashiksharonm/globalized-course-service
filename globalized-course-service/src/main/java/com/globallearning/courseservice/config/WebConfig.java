package com.globallearning.courseservice.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration: CORS and MessageSource.
 *
 * <p>
 * <b>MessageSource:</b> Uses {@link ReloadableResourceBundleMessageSource}
 * loaded from
 * {@code classpath:i18n/messages}. The default encoding is UTF-8 to support
 * Arabic, Japanese,
 * and other non-Latin scripts without requiring escaped unicode sequences in
 * properties files.
 *
 * <p>
 * <b>CORS:</b> Restricted to the frontend origin. In production, this is
 * overridden by the
 * API Gateway / load balancer; the application-level CORS config serves local
 * and staging environments.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:i18n/messages");
        source.setDefaultEncoding("UTF-8");
        // In development, reload message files without restart. Disabled in prod.
        source.setCacheSeconds(Integer.parseInt(System.getProperty("messages.cache.seconds", "-1")));
        return source;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:3000", // local frontend dev server
                        "http://localhost:5173", // Vite dev server
                        "https://*.globallearning.com" // staging / production frontend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
