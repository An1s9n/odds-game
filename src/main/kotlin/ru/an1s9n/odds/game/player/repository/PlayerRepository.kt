package ru.an1s9n.odds.game.player.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.an1s9n.odds.game.player.model.Player
import java.util.UUID

interface PlayerRepository: CoroutineCrudRepository<Player, UUID>
