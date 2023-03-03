package ru.an1s9n.odds.game.player.service

import ru.an1s9n.odds.game.player.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.model.Player
import java.util.UUID

interface PlayerService {

  suspend fun getById(id: UUID): Player?

  /**
   * @throws UsernameAlreadyTakenException if user with specified username already exists in DB
   */
  suspend fun add(player: Player): Player
}
