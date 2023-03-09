package ru.an1s9n.odds.game.auth

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.stereotype.Component

@Component
final class BearerAuthenticationWebFilter(
  reactiveAuthenticationManager: ReactiveAuthenticationManager,
) : AuthenticationWebFilter(reactiveAuthenticationManager) {

  init {
    setServerAuthenticationConverter(BearerServerAuthenticationConverter)
  }
}
