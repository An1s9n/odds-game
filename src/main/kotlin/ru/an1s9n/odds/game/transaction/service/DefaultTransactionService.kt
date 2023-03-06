package ru.an1s9n.odds.game.transaction.service

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.repository.TransactionRepository

@Service
class DefaultTransactionService(
  private val transactionRepository: TransactionRepository,
) : TransactionService {

  override suspend fun add(transaction: Transaction): Transaction = transactionRepository.save(transaction)

  override fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<Transaction> =
    transactionRepository.findAllByPlayerIdOrderByTimestampUtcDesc(player.id!!, PageRequest.of(page - 1, perPage))
}
