package ru.an1s9n.odds.game.player.top.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.player.top.response.PlayerTopProjection

@WebFluxTest(PlayerTopController::class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class PlayerTopControllerTest(
  private val webTestClient: WebTestClient,
  @MockkBean private val mockPlayerService: PlayerService,
) {

  private val testTopPlayer1 = PlayerTopProjection(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", sumPrizeCents = 700)

  private val testTopPlayer2 = PlayerTopProjection(username = "BlahPlayer", firstName = "Blah", lastName = "Ivanov", sumPrizeCents = 600)

  @Test
  internal fun `ensure top endpoint works correctly`() {
    every { mockPlayerService.getTopBySumPrize(1, 20) } returns flowOf(testTopPlayer1, testTopPlayer2)

    webTestClient.get()
      .uri("/player/top")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(PlayerTopProjection::class.java).hasSize(2).contains(testTopPlayer1).contains(testTopPlayer2)
  }

  @Test
  internal fun `ensure top endpoint works correctly with user-specified pagination parameters`() {
    every { mockPlayerService.getTopBySumPrize(3, 1) } returns flowOf(testTopPlayer2)

    webTestClient.get()
      .uri("/player/top?page=3&perPage=1")
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBodyList(PlayerTopProjection::class.java).hasSize(1).contains(testTopPlayer2)
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
