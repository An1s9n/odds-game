package ru.an1s9n.odds.game.transaction.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.transaction.model.Transaction

interface TransactionService {

  suspend fun add(transaction: Transaction): Transaction

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<Transaction>
}
