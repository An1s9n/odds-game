package ru.an1s9n.oddsgame.bet.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface BetRepository : CoroutineCrudRepository<Bet, UUID> {

  fun findAllByPlayerIdOrderByTimestampUtcDesc(playerId: UUID, pageable: Pageable): Flow<Bet>
}
