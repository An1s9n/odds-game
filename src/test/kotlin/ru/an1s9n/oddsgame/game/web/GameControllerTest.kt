package ru.an1s9n.oddsgame.game.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import ru.an1s9n.oddsgame.auth.TEST_AUTH_HEADER
import ru.an1s9n.oddsgame.auth.TEST_AUTH_PLAYER_ID
import ru.an1s9n.oddsgame.auth.TEST_JWT_SECRET
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.config.SecurityConfig
import ru.an1s9n.oddsgame.game.dto.PlayRequestDto
import ru.an1s9n.oddsgame.game.service.GameService
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.util.nowUtc
import ru.an1s9n.oddsgame.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [GameController::class],
  properties = ["app.jwt.secret=$TEST_JWT_SECRET"],
)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class GameControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
  @MockkBean private val mockGameService: GameService,
) {

  private val testPlayer = Player(id = UUID.fromString(TEST_AUTH_PLAYER_ID), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testPlayRequestDto = PlayRequestDto(betNumber = 10, betCredits = 5)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure play endpoint works correctly`() {
    val testBetDto = BetDto(id = UUID.randomUUID(), playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 10, betCents = 500, prizeNumber = 2, prizeCents = 0)
    every { runBlocking { mockGameService.play(testPlayer, testPlayRequestDto) } } returns testBetDto

    webTestClient.post()
      .uri("/game/play")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .bodyValue(testPlayRequestDto)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(BetDto::class.java).isEqualTo(testBetDto)
  }

  @Test
  internal fun `ensure play endpoint returns 400 in case of invalid request ResponseStatusException`() {
    every { runBlocking { mockGameService.play(testPlayer, testPlayRequestDto) } } throws
      ResponseStatusException(HttpStatus.BAD_REQUEST, "some-message")

    webTestClient.post()
      .uri("/game/play")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .bodyValue(testPlayRequestDto)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().jsonPath("$.message").isEqualTo("some-message")
  }

  @Test
  internal fun `ensure play endpoint prohibits unauthenticated access`() {
    webTestClient.post()
      .uri("/game/play")
      .bodyValue(testPlayRequestDto)
      .exchange()
      .expectStatus().isUnauthorized
  }
}
