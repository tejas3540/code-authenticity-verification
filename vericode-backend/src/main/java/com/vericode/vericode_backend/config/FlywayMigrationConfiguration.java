package com.vericode.vericode_backend.config;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FlywayMigrationConfiguration {

    @Bean
    FlywayConfigurationCustomizer safeBaselineForExistingSchemas() {
        return (FluentConfiguration configuration) -> configuration
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .cleanDisabled(true);
    }
}

