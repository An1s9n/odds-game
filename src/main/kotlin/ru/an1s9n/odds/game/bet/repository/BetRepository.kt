package ru.an1s9n.odds.game.bet.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.an1s9n.odds.game.bet.model.Bet
import java.util.UUID

interface BetRepository : CoroutineCrudRepository<Bet, UUID>
