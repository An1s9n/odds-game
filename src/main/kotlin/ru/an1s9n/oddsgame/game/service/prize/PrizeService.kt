package ru.an1s9n.oddsgame.game.service.prize

interface PrizeService {

  fun definePrizeCents(betNumber: Int, prizeNumber: Int, betCents: Long): Long
}
