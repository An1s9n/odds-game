package ru.an1s9n.odds.game.game.range

import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.config.properties.GameProperties

@Service
class DefaultGameRangeService(
  gameProperties: GameProperties,
) : GameRangeService {

  override val gameRange: IntRange = with(gameProperties.range) { leftInclusive..rightInclusive }

  override fun generatePrizeNumber(): Int = gameRange.random()
}
