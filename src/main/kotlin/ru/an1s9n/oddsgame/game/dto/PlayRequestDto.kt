package ru.an1s9n.oddsgame.game.dto

import io.swagger.v3.oas.annotations.media.Schema

data class PlayRequestDto(

  @Schema(description = "any value inside game range", example = "4")
  val betNumber: Int,

  @Schema(description = "player must have enough credits for bet", example = "100")
  val betCredits: Long,
)
