package ru.an1s9n.oddsgame.web.resolver

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.reactive.BindingContext
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.repository.PlayerRepository
import java.util.UUID
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PlayerArgumentResolverTest {

  init {
    mockkStatic(ReactiveSecurityContextHolder::class)
  }

  private val mockPlayerRepository: PlayerRepository = mockk()

  private val playerArgumentResolver: PlayerArgumentResolver = PlayerArgumentResolver(mockPlayerRepository)

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val playerMethodParameter: MethodParameter = MethodParameter(this::funWithPlayerArg.javaMethod!!, 0)

  @AfterAll
  internal fun unmockStaticMethods() {
    unmockkStatic(ReactiveSecurityContextHolder::class)
  }

  @Test
  internal fun `ensure Player class supported`() {
    assertTrue(playerArgumentResolver.supportsParameter(playerMethodParameter))
  }

  @Test
  internal fun `ensure valid player resolves correctly`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(
        SecurityContextImpl().apply {
          authentication = UsernamePasswordAuthenticationToken(
            testPlayer.id!!,
            null,
            AuthorityUtils.NO_AUTHORITIES,
          )
        },
      )
    every { runBlocking { mockPlayerRepository.findById(testPlayer.id!!) } } returns testPlayer

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).expectNext(testPlayer).verifyComplete()
  }

  @Test
  internal fun `ensure unauthorized exception thrown if securityContext missing`() {
    every { ReactiveSecurityContextHolder.getContext() } returns Mono.empty()

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).verifyErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.UNAUTHORIZED }
  }

  @Test
  internal fun `ensure unauthorized exception thrown if authentication missing`() {
    every { ReactiveSecurityContextHolder.getContext() } returns Mono.just(SecurityContextImpl())

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).verifyErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.UNAUTHORIZED }
  }

  @Test
  internal fun `ensure unauthorized exception thrown if player is not authenticated`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(
        SecurityContextImpl().apply {
          authentication = UsernamePasswordAuthenticationToken(
            testPlayer.id!!,
            null,
            AuthorityUtils.NO_AUTHORITIES,
          ).apply { isAuthenticated = false }
        },
      )

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).verifyErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.UNAUTHORIZED }
  }

  @Test
  internal fun `ensure unauthorized exception thrown if principal is not a valid UUID`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(
        SecurityContextImpl().apply {
          authentication = UsernamePasswordAuthenticationToken(
            "not-a-valid-uuid",
            null,
            AuthorityUtils.NO_AUTHORITIES,
          )
        },
      )

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).verifyErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.UNAUTHORIZED }
  }

  @Test
  internal fun `ensure unauthorized exception thrown if player not exists`() {
    every { ReactiveSecurityContextHolder.getContext() } returns
      Mono.just(
        SecurityContextImpl().apply {
          authentication = UsernamePasswordAuthenticationToken(
            testPlayer.id!!,
            null,
            AuthorityUtils.NO_AUTHORITIES,
          )
        },
      )
    every { runBlocking { mockPlayerRepository.findById(testPlayer.id!!) } } returns null

    StepVerifier.create(
      playerArgumentResolver.resolveArgument(
        playerMethodParameter,
        BindingContext(),
        MockServerWebExchange.builder(MockServerHttpRequest.get("mock-url")).build(),
      ),
    ).verifyErrorMatches { it is ResponseStatusException && it.statusCode == HttpStatus.UNAUTHORIZED }
  }

  private fun funWithPlayerArg(@Suppress("unused_parameter") player: Player) {
  }
}
