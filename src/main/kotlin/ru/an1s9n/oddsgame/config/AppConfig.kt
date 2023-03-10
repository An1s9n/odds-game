package ru.an1s9n.oddsgame.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationPropertiesScan
@OpenAPIDefinition(
  info = Info(
    title = "\${app.group-id}:\${app.artifact-id}",
    version = "\${app.version}",
    description = "REST API for simple odds-based game. For more information see sources at https://github.com/An1s9n/odds-game",
  ),
)
class AppConfig
