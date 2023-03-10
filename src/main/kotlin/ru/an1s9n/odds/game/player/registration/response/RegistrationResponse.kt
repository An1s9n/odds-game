package ru.an1s9n.odds.game.player.registration.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.an1s9n.odds.game.player.dto.PlayerDto

data class RegistrationResponse(

  val playerDto: PlayerDto,

  @Schema(example = "Bearer eyJ...Em4")
  val token: String,
)
