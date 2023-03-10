package ru.an1s9n.odds.game.game.service.prize

interface PrizeService {

  fun definePrizeCents(betNumber: Int, prizeNumber: Int, betCents: Long): Long
}
