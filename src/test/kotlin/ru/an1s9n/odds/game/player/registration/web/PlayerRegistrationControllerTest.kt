package ru.an1s9n.odds.game.player.registration.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import ru.an1s9n.odds.game.auth.BearerAuthenticationWebFilter
import ru.an1s9n.odds.game.config.SecurityConfig
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.registration.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.registration.service.RegistrationService
import ru.an1s9n.odds.game.web.exception.InvalidRequestException
import java.util.UUID

@WebFluxTest(PlayerRegistrationController::class)
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
  internal fun `ensure registration endpoint returns 400 in case of UsernameAlreadyTakenException`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequest) } } throws
      UsernameAlreadyTakenException("An1s9n")

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequest)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("UsernameAlreadyTakenException")
  }

  @Test
  internal fun `ensure registration endpoint returns 400 in case of InvalidRequestException`() {
    every { runBlocking { mockRegistrationService.validateRequestAndRegister(testRegistrationRequest) } } throws
      InvalidRequestException(emptyList())

    webTestClient.post()
      .uri("/player/register")
      .bodyValue(testRegistrationRequest)
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("InvalidRequestException")
  }

  @TestConfiguration(proxyBeanMethods = false)
  @Import(SecurityConfig::class)
  @ComponentScan(basePackageClasses = [BearerAuthenticationWebFilter::class])
  internal class PlayerRegistrationControllerTestConfig
}
