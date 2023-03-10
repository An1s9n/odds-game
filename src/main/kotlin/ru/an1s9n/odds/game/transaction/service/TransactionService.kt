package ru.an1s9n.odds.game.transaction.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.transaction.dto.TransactionDto
import ru.an1s9n.odds.game.transaction.repository.Transaction

interface TransactionService {

  suspend fun add(transaction: Transaction): TransactionDto

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<TransactionDto>
}
