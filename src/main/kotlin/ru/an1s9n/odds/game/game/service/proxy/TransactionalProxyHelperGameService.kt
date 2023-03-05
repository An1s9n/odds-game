package ru.an1s9n.odds.game.game.service.proxy

import org.springframework.dao.OptimisticLockingFailureException
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.player.model.Player

interface TransactionalProxyHelperGameService {

  /**
   * @throws OptimisticLockingFailureException if player was updated concurrently
   */
  suspend fun doPlay(player: Player, betNumber: Int, betCents: Long): Bet
}
