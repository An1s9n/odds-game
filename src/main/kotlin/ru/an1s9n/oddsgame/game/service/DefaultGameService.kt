package ru.an1s9n.oddsgame.game.service

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.game.dto.PlayRequestDto
import ru.an1s9n.oddsgame.game.service.proxy.TransactionalProxyHelperGameService
import ru.an1s9n.oddsgame.game.service.range.GameRangeService
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.repository.PlayerRepository

@Service
class DefaultGameService(
  private val playerRepository: PlayerRepository,
  private val gameRangeService: GameRangeService,
  private val transactionalProxyHelperGameService: TransactionalProxyHelperGameService,
) : GameService {

  private val log = LoggerFactory.getLogger(this.javaClass)

  override suspend fun validateRequestAndPlay(player: Player, playRequestDto: PlayRequestDto): BetDto {
    log.info("incoming play request: $playRequestDto from player $player")
    validate(player, playRequestDto)
      ?.let { violations -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, violations.joinToString()) }

    return try {
      transactionalProxyHelperGameService.doPlay(player, playRequestDto.betNumber, playRequestDto.betCredits * 100)
        .also { log.info("game successfully played: $it") }
    } catch (e: OptimisticLockingFailureException) {
      validateRequestAndPlay(playerRepository.findById(player.id!!)!!, playRequestDto)
    }
  }

  private fun validate(player: Player, playRequestDto: PlayRequestDto): List<String>? =
    buildList {
      if (playRequestDto.betCredits <= 0) {
        add("betCredits must be grater than 0")
      }
      if (player.walletCents < playRequestDto.betCredits * 100) {
        add("insufficient wallet: required ${playRequestDto.betCredits * 100} cents, on wallet ${player.walletCents} cents")
      }
      val gameRange = gameRangeService.gameRange
      if (playRequestDto.betNumber !in gameRange) {
        add("betNumber ${playRequestDto.betNumber} is out of $gameRange game range")
      }
    }.ifEmpty { null }
      ?.also { log.info("play request $playRequestDto from player $player is invalid: $it") }
}
