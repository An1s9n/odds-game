package ru.an1s9n.oddsgame.player.service

import kotlinx.coroutines.flow.Flow
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.oddsgame.player.dto.PlayerDto
import ru.an1s9n.oddsgame.player.dto.TopPlayerDto
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.repository.PlayerRepository
import java.util.UUID

@Service
class DefaultPlayerService(
  private val playerRepository: PlayerRepository,
) : PlayerService {

  override suspend fun getById(id: UUID): PlayerDto? = playerRepository.findById(id)?.toDto()

  override suspend fun add(player: Player): PlayerDto =
    try {
      playerRepository.save(player).toDto()
    } catch (e: DuplicateKeyException) {
      throw ResponseStatusException(HttpStatus.BAD_REQUEST, "username ${player.username} is already taken")
    }

  override suspend fun addToWallet(player: Player, addCents: Long): PlayerDto {
    val playerCopy = player.copy()
    playerCopy.walletCents += addCents
    return playerRepository.save(playerCopy).toDto()
  }

  override fun getTopBySumPrize(page: Int, perPage: Int): Flow<TopPlayerDto> =
    playerRepository.findTopByPrizeSum(perPage, (page - 1) * perPage)

  private fun Player.toDto() = PlayerDto(
    id = id,
    username = username,
    firstName = firstName,
    lastName = lastName,
    walletCents = walletCents,
  )
}
