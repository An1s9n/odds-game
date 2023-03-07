package ru.an1s9n.odds.game.bet.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.service.BetService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.util.nowUtc
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(BetController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class BetControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
  @MockkBean private val mockBetService: BetService,
) {

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testBet1 = Bet(playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 5, prizeCents = 50)

  private val testBet2 = Bet(playerId = testPlayer.id!!, timestampUtc = nowUtc().minusMinutes(7), betNumber = 1, betCents = 5, prizeNumber = 7, prizeCents = 0)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure my endpoint works correctly`() {
    every { mockBetService.findAllByPlayerFreshFirst(testPlayer, 1, 20) } returns
      flowOf(testBet1, testBet2)

    webTestClient.get()
      .uri("/bet/my")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Bet::class.java).hasSize(2).contains(testBet1).contains(testBet2)
  }

  @Test
  internal fun `ensure my endpoint works correctly with user-specified pagination parameters`() {
    every { mockBetService.findAllByPlayerFreshFirst(eq(testPlayer), eq(3), eq(1)) } returns flowOf(testBet2)

    webTestClient.get()
      .uri("/bet/my?page=3&perPage=1")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Bet::class.java).hasSize(1).contains(testBet2)
  }

  @Test
  internal fun `ensure my endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/bet/my?page=-1&perPage=270")
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }
}
