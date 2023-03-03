package ru.an1s9n.odds.game.player.registration.response

import ru.an1s9n.odds.game.player.model.Player

data class RegistrationResponse(
  val player: Player,
  val token: String,
)
