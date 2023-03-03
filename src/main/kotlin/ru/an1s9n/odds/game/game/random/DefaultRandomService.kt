package ru.an1s9n.odds.game.game.random

import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.config.properties.GameRangeProperties

@Service
class DefaultRandomService(
  gameRangeProperties: GameRangeProperties,
) : RandomService {

  private val gameRange: IntRange = with(gameRangeProperties) { leftInclusive..rightInclusive }

  override fun generatePrizeNumber(): Int = gameRange.random()
}
