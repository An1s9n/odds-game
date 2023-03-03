package ru.an1s9n.odds.game.bet.service

import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.repository.BetRepository

@Service
class DefaultBetService(
  private val betRepository: BetRepository,
) : BetService {

  override suspend fun add(bet: Bet): Bet = betRepository.save(bet)
}
