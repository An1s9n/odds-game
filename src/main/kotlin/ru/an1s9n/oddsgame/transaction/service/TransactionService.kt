package ru.an1s9n.oddsgame.transaction.service

import kotlinx.coroutines.flow.Flow
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.transaction.dto.TransactionDto
import ru.an1s9n.oddsgame.transaction.repository.Transaction

interface TransactionService {

  suspend fun add(transaction: Transaction): TransactionDto

  fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<TransactionDto>
}
