package ru.an1s9n.odds.game.web.resolver

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.reactive.BindingContext
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import java.util.UUID
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertTrue

internal class PlayerArgumentResolverTest {

  init {
    mockkStatic(ReactiveSecurityContextHolder::class)
  }

  private val mockPlayerService: PlayerService = mockk()

  private val playerArgumentResolver: PlayerArgumentResolver = PlayerArgumentResolver(mockPlayerService)

  private val testPlayer =
    Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val playerMethodParameter: MethodParameter = MethodParameter(this::funWithPlayerArg.javaMethod!!, 0)

  @Test
  internal fun `ensure Player class supported`() {
    assertTrue(playerArgumentResolver.supportsParameter(playerMethodParameter))
  }

  @Test
  internal fun `ensure valid player resolves correctly`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(SecurityContextImpl().apply {
        authentication = UsernamePasswordAuthenticationToken(
          testPlayer.id!!,
          null,
          AuthorityUtils.NO_AUTHORITIES
        )
      })
    every { runBlocking { mockPlayerService.getById(testPlayer.id!!) } } returns testPlayer

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectNext(testPlayer).expectComplete().verify()
  }

  @Test
  internal fun `ensure IllegalStateException thrown if securityContext missing`() {
    every { ReactiveSecurityContextHolder.getContext() } returns Mono.empty()

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectError(IllegalStateException::class.java).verify()
  }

  @Test
  internal fun `ensure IllegalStateException thrown if authentication missing`() {
    every { ReactiveSecurityContextHolder.getContext() } returns Mono.just(SecurityContextImpl())

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectError(IllegalStateException::class.java).verify()
  }

  @Test
  internal fun `ensure IllegalStateException thrown if player is not authenticated`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(SecurityContextImpl().apply {
        authentication = UsernamePasswordAuthenticationToken(
          testPlayer.id!!,
          null,
          AuthorityUtils.NO_AUTHORITIES
        ).apply { isAuthenticated = false }
      })

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectError(IllegalStateException::class.java).verify()
  }

  @Test
  internal fun `ensure IllegalStateException thrown if principal is not a valid UUID`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(SecurityContextImpl().apply {
        authentication = UsernamePasswordAuthenticationToken(
          "not-a-valid-uuid",
          null,
          AuthorityUtils.NO_AUTHORITIES
        )
      })

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectError(IllegalStateException::class.java).verify()
  }

  @Test
  internal fun `ensure IllegalStateException thrown if player not exists`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(SecurityContextImpl().apply {
        authentication = UsernamePasswordAuthenticationToken(
          testPlayer.id!!,
          null,
          AuthorityUtils.NO_AUTHORITIES
        )
      })
    every { runBlocking { mockPlayerService.getById(testPlayer.id!!) } } returns null

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build()
      )
    ).expectError(IllegalStateException::class.java).verify()
  }

  private fun funWithPlayerArg(player: Player) {
  }
}
