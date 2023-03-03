package ru.an1s9n.odds.game.transaction.service

import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.repository.TransactionRepository

@Service
class DefaultTransactionService(
  private val transactionRepository: TransactionRepository,
) : TransactionService {

  override suspend fun add(transaction: Transaction): Transaction = transactionRepository.save(transaction)
}
