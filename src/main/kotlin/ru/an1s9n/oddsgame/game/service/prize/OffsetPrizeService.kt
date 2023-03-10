package ru.an1s9n.oddsgame.game.service.prize

import org.springframework.stereotype.Service
import ru.an1s9n.oddsgame.config.properties.GameProperties
import kotlin.math.abs

@Service
class OffsetPrizeService(
  gameProperties: GameProperties,
) : PrizeService {

  private val offsetToPrizeFun: Map<Int, (Long) -> Long> = gameProperties.offsetToPrizeFun.mapValues { (_, prizeFun) ->
    val op = prizeFun.first()
    val cof = prizeFun.substring(1).toInt()
    when (op) {
      '*' -> { bet -> bet * cof }
      '/' -> { bet -> bet / cof }
      else -> error("unknown operator \"$op\"")
    }
  }

  override fun definePrizeCents(betNumber: Int, prizeNumber: Int, betCents: Long): Long =
    offsetToPrizeFun[abs(betNumber - prizeNumber)]?.invoke(betCents) ?: 0L
}
