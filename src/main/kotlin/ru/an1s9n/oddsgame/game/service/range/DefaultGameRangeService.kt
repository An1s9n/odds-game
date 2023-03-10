package ru.an1s9n.oddsgame.game.service.range

import org.springframework.stereotype.Service
import ru.an1s9n.oddsgame.config.properties.GameProperties

@Service
class DefaultGameRangeService(
  gameProperties: GameProperties,
) : GameRangeService {

  override val gameRange: IntRange = with(gameProperties.range) { leftInclusive..rightInclusive }

  override fun generatePrizeNumber(): Int = gameRange.random()
}
