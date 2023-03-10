package ru.an1s9n.odds.game.game.service.range

interface GameRangeService {

  val gameRange: IntRange

  fun generatePrizeNumber(): Int
}
