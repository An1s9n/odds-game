package ru.an1s9n.odds.game.transaction.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.an1s9n.odds.game.transaction.model.Transaction
import java.util.UUID

interface TransactionRepository : CoroutineCrudRepository<Transaction, UUID>
