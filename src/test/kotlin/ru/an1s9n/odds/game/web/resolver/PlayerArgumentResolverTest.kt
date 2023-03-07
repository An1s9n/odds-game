package ru.an1s9n.odds.game.web.resolver

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.runBlocking
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
import java.util.UUID

@WebFluxTest(PlayerArgumentResolvingTestController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerArgumentResolverTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerService: PlayerService,
  @MockkBean private val mockJwtService: JwtService,
) {

  private val testPlayer = Player(id = UUID.randomUUID(), username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 700)

  @Test
  internal fun `ensure valid user resolves correctly`() {
    every { mockJwtService.validateAndExtractIdFrom("mock-token") } returns testPlayer.id
    every { runBlocking { mockPlayerService.getById(testPlayer.id!!) } } returns testPlayer

    webTestClient.get()
      .uri("/test/resolve-player")
      .header(HttpHeaders.AUTHORIZATION, "mock-token")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody(Player::class.java).isEqualTo(testPlayer)
  }

  @Test
  internal fun `ensure 401 thrown token is not provided`() {
    webTestClient.get()
      .uri("/test/resolve-player")
      .exchange()
      .expectStatus().isUnauthorized
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("UnauthenticatedException")
  }

  @Test
  internal fun `ensure 401 thrown in case of invalid token`() {
    every { mockJwtService.validateAndExtractIdFrom("invalid-mock-token") } returns null

    webTestClient.get()
      .uri("/test/resolve-player")
      .header(HttpHeaders.AUTHORIZATION, "invalid-mock-token")
      .exchange()
      .expectStatus().isUnauthorized
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("UnauthenticatedException")
  }

  @Test
  internal fun `ensure 401 thrown in case of valid token but non-existent user`() {
    every { mockJwtService.validateAndExtractIdFrom("mock-token") } returns UUID.randomUUID()
    every { runBlocking { mockPlayerService.getById(any()) } } returns null

    webTestClient.get()
      .uri("/test/resolve-player")
      .header(HttpHeaders.AUTHORIZATION, "mock-token")
      .exchange()
      .expectStatus().isUnauthorized
      .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
      .expectBody().jsonPath("$.title").isEqualTo("UnauthenticatedException")
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackageClasses = [PlayerArgumentResolver::class])
  internal class PlayerArgumentResolverTestConfig
}
