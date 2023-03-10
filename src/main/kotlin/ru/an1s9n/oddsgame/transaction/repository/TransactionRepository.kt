package ru.an1s9n.oddsgame.transaction.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface TransactionRepository : CoroutineCrudRepository<Transaction, UUID> {

  fun findAllByPlayerIdOrderByTimestampUtcDesc(playerId: UUID, pageable: Pageable): Flow<Transaction>
}
