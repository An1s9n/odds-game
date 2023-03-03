package ru.an1s9n.odds.game.transaction.service

import ru.an1s9n.odds.game.transaction.model.Transaction

interface TransactionService {

  suspend fun add(transaction: Transaction): Transaction
}
