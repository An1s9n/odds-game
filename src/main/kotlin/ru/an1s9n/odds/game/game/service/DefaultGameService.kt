package ru.an1s9n.odds.game.game.service

import org.slf4j.LoggerFactory
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.range.GameRangeService
import ru.an1s9n.odds.game.game.service.proxy.TransactionalProxyHelperGameService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.DefaultPlayerService

@Service
class DefaultGameService(
  private val playerService: DefaultPlayerService,
  private val gameRangeService: GameRangeService,
  private val transactionalProxyHelperGameService: TransactionalProxyHelperGameService,
) : GameService {

  private val log = LoggerFactory.getLogger(this.javaClass)

  override suspend fun validateRequestAndPlay(player: Player, playRequest: PlayRequest): BetDto {
    log.info("incoming play request: $playRequest from player $player")
    validate(player, playRequest)
      ?.let { violations -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, violations.joinToString()) }

    return try {
      transactionalProxyHelperGameService.doPlay(player, playRequest.betNumber, playRequest.betCredits * 100)
        .also { log.info("game successfully played: $it") }
    } catch (e: OptimisticLockingFailureException) {
      validateRequestAndPlay(playerService.getById(player.id!!)!!, playRequest)
    }
  }

  private fun validate(player: Player, playRequest: PlayRequest): List<String>? =
    buildList {
      if (playRequest.betCredits <= 0) {
        add("betCredits must be grater than 0")
      }
      if (player.walletCents < playRequest.betCredits * 100) {
        add("insufficient wallet: required ${playRequest.betCredits * 100} cents, on wallet ${player.walletCents} cents")
      }
      val gameRange = gameRangeService.gameRange
      if (playRequest.betNumber !in gameRange) {
        add("betNumber ${playRequest.betNumber} is out of $gameRange game range")
      }
    }.ifEmpty { null }
      ?.also { log.info("play request $playRequest from player $player is invalid: $it") }
}
