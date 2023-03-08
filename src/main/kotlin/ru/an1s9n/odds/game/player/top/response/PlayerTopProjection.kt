package ru.an1s9n.odds.game.player.top.response

import io.swagger.v3.oas.annotations.media.Schema

data class PlayerTopProjection(

  @Schema(example = "An1s9n") val username: String,

  @Schema(example = "Pavel") val firstName: String,

  @Schema(example = "Anisimov") val lastName: String,

  @Schema(example = "540") val sumPrizeCents: Long,
)
