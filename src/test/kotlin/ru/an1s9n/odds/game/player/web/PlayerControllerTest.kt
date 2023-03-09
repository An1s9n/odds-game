package ru.an1s9n.odds.game.player.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.auth.BearerAuthenticationWebFilter
import ru.an1s9n.odds.game.config.SecurityConfig
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [PlayerController::class],
  properties = ["app.jwt.secret=test-secret"],
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
) {

  private val testPlayer = Player(UUID.fromString("52fbf507-b259-43f6-9750-78c90c4e2dde"), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure me endpoint works correctly`() {
    webTestClient.get()
      .uri("/player/me")
      .header(HttpHeaders.AUTHORIZATION, "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjUyZmJmNTA3LWIyNTktNDNmNi05NzUwLTc4YzkwYzRlMmRkZSJ9.gyDFGtKmdhYehiQeEAM9iB0Jlr41NDMlCP8mRMhPL-A")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(Player::class.java).isEqualTo(testPlayer)
  }

  @Test
  internal fun `ensure me endpoint prohibits unauthenticated access`() {
    webTestClient.get()
      .uri("/player/me")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @TestConfiguration(proxyBeanMethods = false)
  @Import(SecurityConfig::class)
  @ComponentScan(basePackageClasses = [BearerAuthenticationWebFilter::class])
  internal class PlayerControllerTestConfig
}
