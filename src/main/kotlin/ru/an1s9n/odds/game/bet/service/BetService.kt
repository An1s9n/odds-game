package ru.an1s9n.odds.game.bet.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.bet.repository.Bet
import ru.an1s9n.odds.game.player.model.Player

interface BetService {

  suspend fun add(bet: Bet): BetDto

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<BetDto>
}
