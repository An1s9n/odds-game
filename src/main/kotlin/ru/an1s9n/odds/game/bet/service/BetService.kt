package ru.an1s9n.odds.game.bet.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.player.model.Player

interface BetService {

  suspend fun add(bet: Bet): Bet

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<Bet>
}
