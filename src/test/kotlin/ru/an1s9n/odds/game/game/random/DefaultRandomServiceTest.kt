package ru.an1s9n.odds.game.game.random

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class DefaultRandomServiceTest {

  private val randomService: RandomService = DefaultRandomService()

  @Test
  internal fun `ensure random does not fall out of range bounds`() {
    val rangeLeftEdge = 1
    val rangeRightEdge = 5
    repeat(100) {
      val random = randomService.getRandomFrom(rangeLeftEdge..rangeRightEdge)
      assertTrue(random in rangeLeftEdge..rangeRightEdge)
    }
  }
}
