package ru.an1s9n.odds.game.player.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player

@RestController
@RequestMapping("/player")
@Tag(name = "player")
class PlayerController {

  @GetMapping("/me")
  @SecurityRequirement(name = "JWT Authorization")
  @Operation(
    summary = "get currently authenticated player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = Player::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "401", description = "invalid token", content = [Content(schema = Schema(implementation = ProblemDetail::class), mediaType = "application/problem+json")]),
    ],
  )
  suspend fun me(@Parameter(hidden = true) player: Player): Player = player
}
