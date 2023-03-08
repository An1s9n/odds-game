package ru.an1s9n.odds.game.player.service

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.registration.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.player.top.response.PlayerTopProjection
import java.util.UUID

@Service
class DefaultPlayerService(
  private val playerRepository: PlayerRepository,
) : PlayerService {

  override suspend fun getById(id: UUID): Player? = playerRepository.findById(id)

  override suspend fun add(player: Player): Player =
    try {
      playerRepository.save(player)
    } catch (e: DuplicateKeyException) {
      throw UsernameAlreadyTakenException(player.username)
    }

  override suspend fun addToWallet(player: Player, addCents: Long): Player {
    val playerCopy = player.copy()
    playerCopy.walletCents += addCents
    return playerRepository.save(playerCopy)
  }

  override fun getTopBySumPrize(page: Int, perPage: Int): Flow<PlayerTopProjection> =
    playerRepository.findTopByPrizeSum(perPage, (page - 1) * perPage)
}
