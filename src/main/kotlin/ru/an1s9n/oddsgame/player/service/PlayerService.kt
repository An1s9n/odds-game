package ru.an1s9n.oddsgame.player.service

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.OptimisticLockingFailureException
import ru.an1s9n.oddsgame.player.dto.PlayerDto
import ru.an1s9n.oddsgame.player.dto.TopPlayerDto
import ru.an1s9n.oddsgame.player.repository.Player
import java.util.UUID

interface PlayerService {

  suspend fun getById(id: UUID): PlayerDto?

  suspend fun add(player: Player): PlayerDto

  /**
   * @throws OptimisticLockingFailureException if player was updated concurrently
   */
  suspend fun addToWallet(player: Player, addCents: Long): PlayerDto

  fun getTopBySumPrize(page: Int, perPage: Int): Flow<TopPlayerDto>
}
