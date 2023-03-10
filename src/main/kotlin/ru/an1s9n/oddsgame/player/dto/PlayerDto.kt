package ru.an1s9n.oddsgame.player.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class PlayerDto(

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
)
