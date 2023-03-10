package ru.an1s9n.odds.game.bet.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.bet.repository.Bet
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.player.model.Player

@Service
class DefaultBetService(
  private val betRepository: BetRepository,
) : BetService {

  override suspend fun add(bet: Bet): BetDto = betRepository.save(bet).toDto()

  override fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<BetDto> =
    betRepository.findAllByPlayerIdOrderByTimestampUtcDesc(player.id!!, PageRequest.of(page - 1, perPage))
      .map { it.toDto() }

  private fun Bet.toDto() = BetDto(
    id = id,
    playerId = playerId,
    timestampUtc = timestampUtc,
    betNumber = betNumber,
    betCents = betCents,
    prizeNumber = prizeNumber,
    prizeCents = prizeCents,
  )
}
