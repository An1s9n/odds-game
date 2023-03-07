package ru.an1s9n.odds.game.player.registration.request

import io.swagger.v3.oas.annotations.media.Schema

data class RegistrationRequest(

  @Schema(description = "may not be blank", example = "An1s9n")
  val username: String,

  @Schema(description = "may not be blank", example = "Pavel")
  val firstName: String,

  @Schema(description = "may not be blank", example = "Anisimov")
  val lastName: String,
)
