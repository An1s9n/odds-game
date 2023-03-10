package ru.an1s9n.odds.game.auth

import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.auth.jwt.JwtService

class BearerReactiveAuthenticationManager(
  private val jwtService: JwtService,
) : ReactiveAuthenticationManager {

  override fun authenticate(authentication: Authentication): Mono<Authentication> {
    val token = authentication.credentials.toString()
    if (!jwtService.isValid(token)) {
      throw BadCredentialsException("invalid token")
    }
    return Mono.just(
      UsernamePasswordAuthenticationToken(
        jwtService.extractIdFrom(token),
        token,
        AuthorityUtils.NO_AUTHORITIES,
      ),
    )
  }
}
