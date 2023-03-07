package ru.an1s9n.odds.game.bet.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.service.BetService
import ru.an1s9n.odds.game.jwt.JwtService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.util.nowUtc
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

private const val MOCK_TOKEN = "mock-token"

@WebFluxTest(BetController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class BetControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val betService: BetService,
  @MockkBean private val jwtService: JwtService,
  @MockkBean private val playerService: PlayerService,
) {

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testBet1 = Bet(playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 5, prizeCents = 50)

  private val testBet2 = Bet(playerId = testPlayer.id!!, timestampUtc = nowUtc().minusMinutes(7), betNumber = 1, betCents = 5, prizeNumber = 7, prizeCents = 0)

  @BeforeEach
  internal fun initMocks() {
    every { jwtService.validateAndExtractIdFrom(MOCK_TOKEN) } returns testPlayer.id
    every { runBlocking { playerService.getById(eq(testPlayer.id!!)) } } returns testPlayer
  }

  @Test
  internal fun `ensure my endpoint works correctly`() {
    every { betService.findAllByPlayerFreshFirst(eq(testPlayer), eq(1), eq(20)) } returns
      flowOf(testBet1, testBet2)

    webTestClient.get()
      .uri("/bet/my")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Bet::class.java).hasSize(2).contains(testBet1).contains(testBet2)
  }

  @Test
  internal fun `ensure my endpoint works correctly with user-specified pagination parameters`() {
    every { betService.findAllByPlayerFreshFirst(eq(testPlayer), eq(3), eq(1)) } returns flowOf(testBet2)

    webTestClient.get()
      .uri("/bet/my?page=3&perPage=1")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Bet::class.java).hasSize(1).contains(testBet2)
  }

  @Test
  internal fun `ensure my endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/bet/my?page=-1&perPage=270")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackageClasses = [PlayerArgumentResolver::class])
  internal class BetControllerTestConfig
}
