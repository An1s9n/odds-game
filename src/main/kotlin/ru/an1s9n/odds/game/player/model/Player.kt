package ru.an1s9n.odds.game.player.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.util.UUID

data class Player(

  @Id
  val id: UUID? = null,

  val username: String,

  val firstName: String,

  val lastName: String,

  var balanceCents: Long,

  @Version
  var version: Long? = null,
)
