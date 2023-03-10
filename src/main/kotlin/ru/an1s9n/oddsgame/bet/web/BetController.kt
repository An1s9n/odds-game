package ru.an1s9n.oddsgame.bet.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlinx.coroutines.flow.Flow
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.bet.service.BetService
import ru.an1s9n.oddsgame.player.repository.Player

@RestController
@RequestMapping("/bet")
@Tag(name = "bet")
@Validated
class BetController(
  private val betService: BetService,
) {

  @GetMapping("/my")
  @SecurityRequirement(name = "JWT Authorization")
  @Operation(
    summary = "get bets of currently authenticated player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(array = ArraySchema(schema = Schema(implementation = BetDto::class)), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid request", content = [Content()]),
      ApiResponse(responseCode = "401", description = "invalid token", content = [Content()]),
    ],
  )
  fun my(
    @Parameter(hidden = true) player: Player,
    @Min(1)
    @RequestParam(name = "page", defaultValue = "1")
    page: Int,
    @Min(1)
    @Max(100)
    @RequestParam(name = "perPage", defaultValue = "20")
    perPage: Int,
  ): Flow<BetDto> = betService.findAllByPlayerFreshFirst(player, page, perPage)
}
