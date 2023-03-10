package ru.an1s9n.odds.game.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import ru.an1s9n.odds.game.auth.BearerAuthenticationWebFilter
import ru.an1s9n.odds.game.auth.BearerReactiveAuthenticationManager
import ru.an1s9n.odds.game.auth.jwt.Auth0JwtService
import ru.an1s9n.odds.game.auth.jwt.JwtService

@Configuration
@SecurityScheme(
  type = SecuritySchemeType.HTTP,
  scheme = "Bearer",
  name = "JWT Authorization",
  description = "Enter JWT Bearer token only",
)
class SecurityConfig(
  @Value("\${app.jwt.secret}") private val jwtSecret: String,
) {

  @Bean
  fun securityWebFilterChain(serverHttpSecurity: ServerHttpSecurity): SecurityWebFilterChain =
    serverHttpSecurity
      .csrf { it.disable() }
      .formLogin { it.disable() }
      .httpBasic { it.authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)) }
      .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
      .addFilterAt(bearerAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
      .authorizeExchange { it
        .pathMatchers(
          "/player/register",
          "/player/top",
          "/swagger-ui.html",
          "/v3/api-docs/**",
          "/webjars/swagger-ui/**"
        ).permitAll()
        .anyExchange().authenticated()
      }
      .build()

  @Bean
  fun jwtService(): JwtService = Auth0JwtService(jwtSecret)

  private fun bearerAuthenticationWebFilter(): AuthenticationWebFilter =
    BearerAuthenticationWebFilter(BearerReactiveAuthenticationManager(jwtService()))
}
