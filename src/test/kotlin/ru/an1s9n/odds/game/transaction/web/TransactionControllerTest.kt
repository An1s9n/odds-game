package ru.an1s9n.odds.game.transaction.web

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
import ru.an1s9n.odds.game.jwt.JwtService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.model.TransactionType
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.nowUtc
import ru.an1s9n.odds.game.web.resolver.PlayerArgumentResolver
import java.util.UUID

private const val MOCK_TOKEN = "mock-token"

@WebFluxTest(TransactionController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class TransactionControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val transactionService: TransactionService,
  @MockkBean private val jwtService: JwtService,
  @MockkBean private val playerService: PlayerService,
) {

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testTransaction1 = Transaction(playerId = testPlayer.id!!, timestampUtc = nowUtc(), amountCents = 50, type = TransactionType.PRIZE)

  private val testTransaction2 = Transaction(playerId = testPlayer.id!!, timestampUtc = nowUtc().minusMinutes(7), amountCents = -40, type = TransactionType.BET)

  @BeforeEach
  internal fun initMocks() {
    every { jwtService.validateAndExtractIdFrom(MOCK_TOKEN) } returns testPlayer.id
    every { runBlocking { playerService.getById(eq(testPlayer.id!!)) } } returns testPlayer
  }

  @Test
  internal fun `ensure my endpoint works correctly`() {
    every { transactionService.findAllByPlayerFreshFirst(eq(testPlayer), eq(1), eq(20)) } returns
      flowOf(testTransaction1, testTransaction2)

    webTestClient.get()
      .uri("/transaction/my")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Transaction::class.java).hasSize(2).contains(testTransaction1).contains(testTransaction2)
  }

  @Test
  internal fun `ensure my endpoint works correctly with user-specified pagination parameters`() {
    every { transactionService.findAllByPlayerFreshFirst(eq(testPlayer), eq(3), eq(1)) } returns
      flowOf(testTransaction2)

    webTestClient.get()
      .uri("/transaction/my?page=3&perPage=1")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(Transaction::class.java).hasSize(1).contains(testTransaction2)
  }

  @Test
  internal fun `ensure my endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/transaction/my?page=-1&perPage=270")
      .header(HttpHeaders.AUTHORIZATION, MOCK_TOKEN)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackageClasses = [PlayerArgumentResolver::class])
  internal class TransactionControllerTestConfig
}
