package ru.an1s9n.oddsgame.bet.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.an1s9n.oddsgame.auth.TEST_AUTH_HEADER
import ru.an1s9n.oddsgame.auth.TEST_AUTH_PLAYER_ID
import ru.an1s9n.oddsgame.auth.TEST_JWT_SECRET
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.bet.service.BetService
import ru.an1s9n.oddsgame.config.SecurityConfig
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.util.nowUtc
import ru.an1s9n.oddsgame.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [BetController::class],
  properties = ["app.jwt.secret=$TEST_JWT_SECRET"],
)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class BetControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
  @MockkBean private val mockBetService: BetService,
) {

  private val testPlayer = Player(id = UUID.fromString(TEST_AUTH_PLAYER_ID), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testBetDto1 = BetDto(playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 5, prizeCents = 50)

  private val testBetDto2 = BetDto(playerId = testPlayer.id!!, timestampUtc = nowUtc().minusMinutes(7), betNumber = 1, betCents = 5, prizeNumber = 7, prizeCents = 0)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure my endpoint works correctly`() {
    every { mockBetService.findAllByPlayerFreshFirst(testPlayer, 1, 20) } returns
      flowOf(testBetDto1, testBetDto2)

    webTestClient.get()
      .uri("/bet/my")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(BetDto::class.java).hasSize(2).contains(testBetDto1).contains(testBetDto2)
  }

  @Test
  internal fun `ensure my endpoint works correctly with user-specified pagination parameters`() {
    every { mockBetService.findAllByPlayerFreshFirst(testPlayer, 3, 1) } returns flowOf(testBetDto2)

    webTestClient.get()
      .uri("/bet/my?page=3&perPage=1")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(BetDto::class.java).hasSize(1).contains(testBetDto2)
  }

  @Test
  internal fun `ensure my endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/bet/my?page=-1&perPage=270")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }

  @Test
  internal fun `ensure my endpoint prohibits unauthenticated access`() {
    webTestClient.get()
      .uri("/bet/my")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
