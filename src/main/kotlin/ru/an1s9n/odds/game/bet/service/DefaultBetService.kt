package ru.an1s9n.odds.game.bet.service

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.player.model.Player

@Service
class DefaultBetService(
  private val betRepository: BetRepository,
) : BetService {

  override suspend fun add(bet: Bet): Bet = betRepository.save(bet)

  override fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<Bet> =
    betRepository.findAllByPlayerIdOrderByTimestampUtcDesc(player.id!!, PageRequest.of(page - 1, perPage))
}
