package com.retailpos.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.DateTimeFeature;

/**
 * Jackson customization: serialize dates/times as ISO-8601 strings rather than numeric
 * timestamps.
 *
 * <p>Spring Boot 4.x ships a Jackson 3 ({@code tools.jackson.*}) JSON stack by default (the
 * classic Spring Boot {@code Jackson2ObjectMapperBuilderCustomizer} type from Boot 2/3 no longer
 * exists). In Jackson 3, the "write dates as timestamps" toggle moved off
 * {@code SerializationFeature} onto the dedicated {@link DateTimeFeature} enum. This is also set
 * redundantly via the equivalent {@code spring.jackson.datatype.datetime.write-dates-as-timestamps}
 * property in application.yml; both simply disable the same flag.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
