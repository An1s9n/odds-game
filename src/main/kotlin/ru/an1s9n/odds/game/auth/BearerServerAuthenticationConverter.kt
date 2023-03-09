package ru.an1s9n.odds.game.auth

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

object BearerServerAuthenticationConverter : ServerAuthenticationConverter {

  override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
    return Mono.justOrEmpty(exchange.request.headers[HttpHeaders.AUTHORIZATION]?.firstOrNull())
      .filter { it.startsWith(BEARER_PREFIX) }
      .map { authHeader ->
        UsernamePasswordAuthenticationToken(null, authHeader.substring(BEARER_PREFIX.length))
      }
  }
}
