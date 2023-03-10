package ru.an1s9n.oddsgame.player.web

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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.oddsgame.player.dto.PlayerDto
import ru.an1s9n.oddsgame.player.dto.TopPlayerDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationResponseDto
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.service.PlayerService
import ru.an1s9n.oddsgame.player.service.registration.RegistrationService

@RestController
@RequestMapping("/player")
@Tag(name = "player")
@Validated
class PlayerController(
  private val playerService: PlayerService,
  private val registrationService: RegistrationService,
) {

  @PostMapping("/register")
  @Operation(
    summary = "register new player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = RegistrationResponseDto::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid registrationRequest", content = [Content()]),
    ],
  )
  suspend fun register(@RequestBody registrationRequestDto: RegistrationRequestDto): RegistrationResponseDto =
    registrationService.validateRequestAndRegister(registrationRequestDto)

  @GetMapping("/me")
  @SecurityRequirement(name = "JWT Authorization")
  @Operation(
    summary = "get currently authenticated player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = PlayerDto::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "401", description = "invalid token", content = [Content()]),
    ],
  )
  suspend fun me(@Parameter(hidden = true) player: Player): Player = player

  @GetMapping("/top")
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
