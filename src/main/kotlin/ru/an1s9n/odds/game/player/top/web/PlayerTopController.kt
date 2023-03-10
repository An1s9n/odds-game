package ru.an1s9n.odds.game.player.top.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlinx.coroutines.flow.Flow
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.dto.TopPlayerDto
import ru.an1s9n.odds.game.player.service.PlayerService

@RestController
@RequestMapping("/player/top")
@Tag(name = "player")
@Validated
class PlayerTopController(
  private val playerService: PlayerService,
) {

  @GetMapping
  @Operation(
    summary = "get top players ranked by total winnings",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(array = ArraySchema(schema = Schema(implementation = TopPlayerDto::class)), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid request", content = [Content()]),
    ],
  )
  fun top(
    @Min(1)
    @RequestParam(name = "page", defaultValue = "1")
    page: Int,
    @Min(1)
    @Max(100)
    @RequestParam(name = "perPage", defaultValue = "20")
    perPage: Int,
  ): Flow<TopPlayerDto> = playerService.getTopBySumPrize(page, perPage)
}
