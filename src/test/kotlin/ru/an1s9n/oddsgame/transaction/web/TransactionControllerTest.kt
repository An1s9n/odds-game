package ru.an1s9n.oddsgame.transaction.web

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
import ru.an1s9n.oddsgame.config.SecurityConfig
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.transaction.dto.TransactionDto
import ru.an1s9n.oddsgame.transaction.dto.TransactionType
import ru.an1s9n.oddsgame.transaction.service.TransactionService
import ru.an1s9n.oddsgame.util.nowUtc
import ru.an1s9n.oddsgame.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [TransactionController::class],
  properties = ["app.jwt.secret=$TEST_JWT_SECRET"],
)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class TransactionControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
  @MockkBean private val mockTransactionService: TransactionService,
) {

  private val testPlayer = Player(id = UUID.fromString(TEST_AUTH_PLAYER_ID), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testTransactionDto1 = TransactionDto(playerId = testPlayer.id!!, timestampUtc = nowUtc(), amountCents = 50, type = TransactionType.PRIZE)

  private val testTransactionDto2 = TransactionDto(playerId = testPlayer.id!!, timestampUtc = nowUtc().minusMinutes(7), amountCents = -40, type = TransactionType.BET)

  @BeforeEach
  internal fun initMocks() {
    every { mockPlayerArgumentResolver.supportsParameter(match { it.parameterType == Player::class.java }) } returns true
    every { mockPlayerArgumentResolver.resolveArgument(any(), any(), any()) } returns Mono.just(testPlayer)
  }

  @Test
  internal fun `ensure my endpoint works correctly`() {
    every { mockTransactionService.findAllByPlayerFreshFirst(testPlayer, 1, 20) } returns
      flowOf(testTransactionDto1, testTransactionDto2)

    webTestClient.get()
      .uri("/transaction/my")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(TransactionDto::class.java).hasSize(2).contains(testTransactionDto1).contains(testTransactionDto2)
  }

  @Test
  internal fun `ensure my endpoint works correctly with user-specified pagination parameters`() {
    every { mockTransactionService.findAllByPlayerFreshFirst(testPlayer, 3, 1) } returns
      flowOf(testTransactionDto2)

    webTestClient.get()
      .uri("/transaction/my?page=3&perPage=1")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(TransactionDto::class.java).hasSize(1).contains(testTransactionDto2)
  }

  @Test
  internal fun `ensure my endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/transaction/my?page=-1&perPage=270")
      .header(HttpHeaders.AUTHORIZATION, TEST_AUTH_HEADER)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }

  @Test
  internal fun `ensure my endpoint prohibits unauthenticated access`() {
    webTestClient.get()
      .uri("/transaction/my")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
