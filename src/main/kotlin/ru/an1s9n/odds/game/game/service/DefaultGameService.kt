package ru.an1s9n.odds.game.game.service

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.range.GameRangeService
import ru.an1s9n.odds.game.game.service.proxy.TransactionalProxyHelperGameService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.DefaultPlayerService
import ru.an1s9n.odds.game.web.exception.InvalidRequestException

@Service
class DefaultGameService(
  private val playerService: DefaultPlayerService,
  private val gameRangeService: GameRangeService,
  private val transactionalProxyHelperGameService: TransactionalProxyHelperGameService,
) : GameService {

  override suspend fun validateRequestAndPlay(player: Player, playRequest: PlayRequest): Bet {
    val walletCents = player.walletCents
    val betCents = playRequest.betCredits * 100
    val betNumber = playRequest.betNumber

    validate(walletCents, betCents, betNumber)?.let { violations -> throw InvalidRequestException(violations) }

    return try {
      transactionalProxyHelperGameService.doPlay(player, betNumber, betCents)
    } catch (e: OptimisticLockingFailureException) {
      validateRequestAndPlay(playerService.getById(player.id!!)!!, playRequest)
    }
  }

  private fun validate(walletCents: Long, betCents: Long, betNumber: Int): List<String>? =
    buildList {
      if (walletCents < betCents) {
        add("insufficient wallet: required $betCents cents, on wallet $walletCents cents")
      }
      val gameRange = gameRangeService.gameRange
      if (betNumber !in gameRange) {
        add("betNumber $betNumber is out of $gameRange game range")
      }
    }.ifEmpty { null }
}
