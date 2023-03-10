package ru.an1s9n.odds.game.transaction.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.transaction.dto.TransactionDto

interface TransactionService {

  suspend fun add(transactionDto: TransactionDto): TransactionDto

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<TransactionDto>
}
