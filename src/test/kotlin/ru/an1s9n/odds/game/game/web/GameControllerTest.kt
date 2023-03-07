package ru.an1s9n.odds.game.game.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.service.GameService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.util.nowUtc
import ru.an1s9n.odds.game.web.exception.InvalidRequestException
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(GameController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class GameControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
  @MockkBean private val mockGameService: GameService,
) {

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testPlayRequest = PlayRequest(betNumber = 10, betCredits = 5)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure play endpoint works correctly`() {
    val testBet = Bet(id = UUID.randomUUID(), playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 10, betCents = 500, prizeNumber = 2, prizeCents = 0)
    every { runBlocking { mockGameService.validateRequestAndPlay(testPlayer, testPlayRequest) } } returns testBet

    webTestClient.post()
      .uri("/game/play")
      .bodyValue(testPlayRequest)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(Bet::class.java).isEqualTo(testBet)
  }

  @Test
  internal fun `ensure play endpoint returns 400 in case of InvalidRequestException`() {
    every { runBlocking { mockGameService.validateRequestAndPlay(testPlayer, testPlayRequest) } } throws
      InvalidRequestException(emptyList())

    webTestClient.post()
      .uri("/game/play")
      .bodyValue(testPlayRequest)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("InvalidRequestException")
  }
}
