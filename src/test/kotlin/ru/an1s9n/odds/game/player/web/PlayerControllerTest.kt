package ru.an1s9n.odds.game.player.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.auth.TEST_AUTH_HEADER
import ru.an1s9n.odds.game.auth.TEST_AUTH_PLAYER_ID
import ru.an1s9n.odds.game.auth.TEST_JWT_SECRET
import ru.an1s9n.odds.game.config.SecurityConfig
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [PlayerController::class],
  properties = ["app.jwt.secret=$TEST_JWT_SECRET"],
)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
) {

  private val testPlayer = Player(UUID.fromString(TEST_AUTH_PLAYER_ID), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure me endpoint works correctly`() {
    webTestClient.get()
      .uri("/player/me")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
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
}
