package ru.an1s9n.odds.game.player.registration.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.odds.game.config.SecurityConfig
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.registration.service.RegistrationService
import java.util.UUID

@WebFluxTest(PlayerRegistrationController::class)
@Import(SecurityConfig::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerRegistrationControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockRegistrationService: RegistrationService,
) {

  private val testRegistrationRequest = RegistrationRequest(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov")

  @Test
  internal fun `ensure registration endpoint works correctly`() {
    val testRegistrationResponse = RegistrationResponse(
      player = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 5),
      token = "mock-token",
    )
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequest) } } returns
      testRegistrationResponse

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequest)
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(RegistrationResponse::class.java).isEqualTo(testRegistrationResponse)
  }

  @Test
  internal fun `ensure registration endpoint returns 400 if username is already taken`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequest) } } throws
      ResponseStatusException(HttpStatus.BAD_REQUEST, "username An1s9n is already taken")

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequest)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().jsonPath("$.message").isEqualTo("username An1s9n is already taken")
  }

  @Test
  internal fun `ensure registration endpoint returns 400 in case of invalid request ResponseStatusException`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequest) } } throws
      ResponseStatusException(HttpStatus.BAD_REQUEST, "some-message")

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequest)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().jsonPath("$.message").isEqualTo("some-message")
  }
}
