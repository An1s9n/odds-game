package ru.an1s9n.odds.game.game.random

import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.config.properties.GameProperties

@Service
class DefaultRandomService(
  gameProperties: GameProperties,
) : RandomService {

  private val gameRange: IntRange = with(gameProperties.range) { leftInclusive..rightInclusive }

  override fun generatePrizeNumber(): Int = gameRange.random()
}
