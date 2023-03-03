package ru.an1s9n.odds.game.player.model.request

data class RegistrationRequest(
  val username: String,
  val firstName: String,
  val lastName: String,
)
