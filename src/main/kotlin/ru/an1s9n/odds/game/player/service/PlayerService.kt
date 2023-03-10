package ru.an1s9n.odds.game.player.service

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.OptimisticLockingFailureException
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.top.response.PlayerTopProjection
import java.util.UUID

interface PlayerService {

  suspend fun getById(id: UUID): Player?

  suspend fun add(player: Player): Player

  /**
   * @throws OptimisticLockingFailureException if player was updated concurrently
   */
  suspend fun addToWallet(player: Player, addCents: Long): Player

  fun getTopBySumPrize(page: Int, perPage: Int): Flow<PlayerTopProjection>
}
