package ru.an1s9n.odds.game.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
@SecurityScheme(
  type = SecuritySchemeType.HTTP,
  scheme = "Bearer",
  name = "JWT Authorization",
  description = "Enter JWT Bearer token only",
)
class AppConfig
