package ru.an1s9n.odds.game.game.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.service.GameService
import ru.an1s9n.odds.game.player.model.Player

@RestController
@RequestMapping("/game")
@Tag(name = "game")
class GameController(
  private val gameService: GameService,
) {

  @PostMapping("/play")
  @SecurityRequirement(name = "JWT Authorization")
  @Operation(
    summary = "bet on a number and play the game",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = BetDto::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid playRequest", content = [Content()]),
      ApiResponse(responseCode = "401", description = "invalid token", content = [Content()]),
    ],
  )
  suspend fun play(@Parameter(hidden = true) player: Player, @RequestBody playRequest: PlayRequest): BetDto =
    gameService.validateRequestAndPlay(player, playRequest)
}
