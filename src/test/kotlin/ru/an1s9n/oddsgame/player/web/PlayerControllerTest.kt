package ru.an1s9n.oddsgame.player.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
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
import ru.an1s9n.oddsgame.config.SecurityConfig
import ru.an1s9n.oddsgame.player.dto.PlayerDto
import ru.an1s9n.oddsgame.player.dto.TopPlayerDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationResponseDto
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.service.PlayerService
import ru.an1s9n.oddsgame.player.service.registration.RegistrationService
import ru.an1s9n.oddsgame.web.resolver.PlayerArgumentResolver
import java.util.UUID

@WebFluxTest(
  controllers = [PlayerController::class],
  properties = ["app.jwt.secret=$TEST_JWT_SECRET"],
)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerService: PlayerService,
  @MockkBean private val mockRegistrationService: RegistrationService,
  @MockkBean private val mockPlayerArgumentResolver: PlayerArgumentResolver,
) {

  @Test
  internal fun `ensure registration endpoint works correctly`() {
    val testRegistrationResponseDto = RegistrationResponseDto(
      playerDto = PlayerDto(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 5),
      token = "mock-token",
    )
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequestDto) } } returns
      testRegistrationResponseDto

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequestDto)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(RegistrationResponseDto::class.java).isEqualTo(testRegistrationResponseDto)
  }

  @Test
  internal fun `ensure registration endpoint returns 400 if username is already taken`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequestDto) } } throws
      ResponseStatusException(HttpStatus.BAD_REQUEST, "username An1s9n is already taken")

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequestDto)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().jsonPath("$.message").isEqualTo("username An1s9n is already taken")
  }

  @Test
  internal fun `ensure registration endpoint returns 400 in case of invalid request ResponseStatusException`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequestDto) } } throws
      ResponseStatusException(HttpStatus.BAD_REQUEST, "some-message")

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequestDto)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().jsonPath("$.message").isEqualTo("some-message")
  }

  private val testRegistrationRequestDto = RegistrationRequestDto(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov")

  private val testPlayer = Player(UUID.fromString(TEST_AUTH_PLAYER_ID), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  private val testTopPlayer1 = TopPlayerDto(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", sumPrizeCents = 700)

  private val testTopPlayer2 = TopPlayerDto(username = "BlahPlayer", firstName = "Blah", lastName = "Ivanov", sumPrizeCents = 600)

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

  @Test
  internal fun `ensure top endpoint works correctly`() {
    every { mockPlayerService.getTopBySumPrize(1, 20) } returns flowOf(testTopPlayer1, testTopPlayer2)

    webTestClient.get()
      .uri("/player/top")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(TopPlayerDto::class.java).hasSize(2).contains(testTopPlayer1).contains(testTopPlayer2)
  }

  @Test
  internal fun `ensure top endpoint works correctly with user-specified pagination parameters`() {
    every { mockPlayerService.getTopBySumPrize(3, 1) } returns flowOf(testTopPlayer2)

    webTestClient.get()
      .uri("/player/top?page=3&perPage=1")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(TopPlayerDto::class.java).hasSize(1).contains(testTopPlayer2)
  }

  @Test
  internal fun `ensure top endpoint prohibits invalid pagination parameters`() {
    webTestClient.get()
      .uri("/player/top?page=-1&perPage=270")
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("ConstraintViolationException")
  }
}
