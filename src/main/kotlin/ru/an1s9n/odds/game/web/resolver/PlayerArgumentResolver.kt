package ru.an1s9n.odds.game.web.resolver

import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import java.util.UUID

@Component
class PlayerArgumentResolver(
  private val playerRepository: PlayerRepository,
) : HandlerMethodArgumentResolver {

  private val log = LoggerFactory.getLogger(this.javaClass)

  override fun supportsParameter(parameter: MethodParameter): Boolean =
    parameter.parameterType == Player::class.java

  override fun resolveArgument(
    parameter: MethodParameter,
    bindingContext: BindingContext,
    exchange: ServerWebExchange,
  ): Mono<Any> =
    ReactiveSecurityContextHolder.getContext()
      .mapNotNull { securityContext -> securityContext.authentication }
      .filter { authentication -> authentication.isAuthenticated }
      .mapNotNull { authentication -> authentication.principal as? UUID }
      .flatMap { playerId -> mono<Any> { playerRepository.findById(playerId!!) } }
      .switchIfEmpty(
        Mono.defer {
          log.warn("failed to resolve player, request headers: ${exchange.request.headers}")
          Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED))
        },
      )
}
