package ru.an1s9n.oddsgame.bet.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.bet.repository.Bet
import ru.an1s9n.oddsgame.bet.repository.BetRepository
import ru.an1s9n.oddsgame.player.repository.Player

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
