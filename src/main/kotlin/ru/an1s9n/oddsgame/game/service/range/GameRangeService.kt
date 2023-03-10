package ru.an1s9n.oddsgame.game.service.range

interface GameRangeService {

  val gameRange: IntRange

  fun generatePrizeNumber(): Int
}
