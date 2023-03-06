package ru.an1s9n.odds.game.web.resolver

import kotlinx.coroutines.reactor.mono
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.jwt.JwtService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.web.exception.UnauthenticatedException

@Component
class PlayerArgumentResolver(
  private val jwtService: JwtService,
  private val playerService: PlayerService,
) : HandlerMethodArgumentResolver {

  override fun supportsParameter(parameter: MethodParameter): Boolean =
    parameter.parameterType == Player::class.java

  override fun resolveArgument(
    parameter: MethodParameter,
    bindingContext: BindingContext,
    exchange: ServerWebExchange
  ): Mono<Any> {
    return Mono.defer {
      exchange.request.headers[HttpHeaders.AUTHORIZATION]?.firstOrNull()
        ?.let { token -> jwtService.validateAndExtractIdFrom(token) }
        ?.let { id -> mono { playerService.getById(id) } }
        ?: Mono.error(UnauthenticatedException())
    }
  }
}
