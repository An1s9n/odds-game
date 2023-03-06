package ru.an1s9n.odds.game.player.top.response

data class PlayerTopProjection (

  val username: String,

  val firstName: String,

  val lastName: String,

  val sumPrizeCents: Long,
)
