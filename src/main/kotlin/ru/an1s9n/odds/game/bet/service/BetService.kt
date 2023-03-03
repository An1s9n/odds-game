package ru.an1s9n.odds.game.bet.service

import ru.an1s9n.odds.game.bet.model.Bet

interface BetService {

  suspend fun add(bet: Bet): Bet
}
