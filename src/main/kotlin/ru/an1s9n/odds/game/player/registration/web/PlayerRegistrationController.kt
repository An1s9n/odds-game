package ru.an1s9n.odds.game.player.registration.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.registration.service.RegistrationService

@RestController
@RequestMapping("/player/register")
@Tag(name = "player")
class PlayerRegistrationController(
  private val registrationService: RegistrationService,
) {

  @PostMapping
  @Operation(
    summary = "register new player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = RegistrationResponse::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid registrationRequest", content = [Content(schema = Schema(implementation = ProblemDetail::class), mediaType = "application/problem+json")]),
    ],
  )
  suspend fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse =
    registrationService.validateRequestAndRegister(registrationRequest)
}
