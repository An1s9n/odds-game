package ru.an1s9n.odds.game.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationPropertiesScan
@OpenAPIDefinition(
  info = Info(
    title = "\${app.group-id}:\${app.artifact-id}",
    version = "\${app.version}",
    description = "REST API for simple odds-based game. For more information see sources at https://github.com/An1s9n/odds-game"
  )
)
@SecurityScheme(
  type = SecuritySchemeType.HTTP,
  scheme = "Bearer",
  name = "JWT Authorization",
  description = "Enter JWT Bearer token only",
)
class AppConfig
