package ru.an1s9n.odds.game.player.model

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.util.UUID

data class Player(

  @Id
  @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  val id: UUID? = null,

  @Schema(example = "An1s9n")
  val username: String,

  @Schema(example = "Pavel")
  val firstName: String,

  @Schema(example = "Anisimov")
  val lastName: String,

  @Schema(example = "100000")
  var walletCents: Long,

  @Version
  @JsonIgnore
  var version: Long? = null,
)
