package ru.an1s9n.odds.game.game.range

interface GameRangeService {

  val gameRange: IntRange

  fun generatePrizeNumber(): Int
}
