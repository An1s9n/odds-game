package ru.an1s9n.odds.game.auth

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.web.server.authentication.AuthenticationWebFilter

class BearerAuthenticationWebFilter(
  reactiveAuthenticationManager: ReactiveAuthenticationManager,
) : AuthenticationWebFilter(reactiveAuthenticationManager) {

  init {
    super.setServerAuthenticationConverter(BearerServerAuthenticationConverter)
  }
}
