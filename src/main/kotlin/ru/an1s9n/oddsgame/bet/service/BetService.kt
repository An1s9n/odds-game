package ru.an1s9n.oddsgame.bet.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.bet.repository.Bet
import ru.an1s9n.oddsgame.player.repository.Player

interface BetService {

  suspend fun add(bet: Bet): BetDto

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<BetDto>
}
