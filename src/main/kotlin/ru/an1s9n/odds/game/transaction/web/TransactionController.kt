package ru.an1s9n.odds.game.transaction.web

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive
import kotlinx.coroutines.flow.Flow
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.service.TransactionService

@RestController
@RequestMapping("/transaction")
@Validated
class TransactionController(
  private val transactionService: TransactionService,
) {

  @GetMapping("/my")
  fun my(
    player: Player,
    @Positive @RequestParam(defaultValue = "1") page: Int,
    @Positive @Max(100) @RequestParam(defaultValue = "20") perPage: Int,
  ): Flow<Transaction> = transactionService.findAllByPlayerFreshFirst(player, page, perPage)
}
