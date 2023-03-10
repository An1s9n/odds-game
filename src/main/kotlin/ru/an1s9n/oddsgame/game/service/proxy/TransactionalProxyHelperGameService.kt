package ru.an1s9n.oddsgame.game.service.proxy

import org.springframework.dao.OptimisticLockingFailureException
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.player.repository.Player

interface TransactionalProxyHelperGameService {

  /**
   * @throws OptimisticLockingFailureException if player was updated concurrently
   */
  suspend fun doPlay(player: Player, betNumber: Int, betCents: Long): BetDto
}
