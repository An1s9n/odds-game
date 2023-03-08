package ru.an1s9n.odds.game.game.service.proxy

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.service.BetService
import ru.an1s9n.odds.game.game.prize.PrizeService
import ru.an1s9n.odds.game.game.range.GameRangeService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.model.TransactionType
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.nowUtc

@Service
class DefaultTransactionalProxyHelperGameService(
  private val playerService: PlayerService,
  private val transactionService: TransactionService,
  private val betService: BetService,
  private val gameRangeService: GameRangeService,
  private val prizeService: PrizeService,
) : TransactionalProxyHelperGameService {

  @Transactional
  override suspend fun doPlay(player: Player, betNumber: Int, betCents: Long): Bet {
    transactionService.add(
      Transaction(
        playerId = player.id!!,
        timestampUtc = nowUtc(),
        amountCents = -betCents,
        type = TransactionType.BET,
      ),
    )
    val prizeNumber = gameRangeService.generatePrizeNumber()
    val prizeCents = prizeService.definePrizeCents(betNumber, prizeNumber, betCents)
    if (prizeCents > 0) {
      transactionService.add(
        Transaction(
          playerId = player.id,
          timestampUtc = nowUtc(),
          amountCents = prizeCents,
          type = TransactionType.PRIZE,
        ),
      )
    }
    playerService.addToWallet(player, prizeCents - betCents)
    return betService.add(
      Bet(
        playerId = player.id,
        timestampUtc = nowUtc(),
        betNumber = betNumber,
        betCents = betCents,
        prizeNumber = prizeNumber,
        prizeCents = prizeCents,
      ),
    )
  }
}
