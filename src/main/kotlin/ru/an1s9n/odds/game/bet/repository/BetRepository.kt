package ru.an1s9n.odds.game.bet.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.an1s9n.odds.game.bet.model.Bet
import java.util.UUID

interface BetRepository : CoroutineCrudRepository<Bet, UUID> {

  fun findAllByPlayerIdOrderByTimestampUtcDesc(playerId: UUID, pageable: Pageable): Flow<Bet>
}
