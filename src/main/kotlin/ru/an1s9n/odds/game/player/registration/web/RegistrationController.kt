package ru.an1s9n.odds.game.player.registration.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.registration.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.registration.service.RegistrationService

@RestController
@RequestMapping("/player")
class RegistrationController(
  private val registrationService: RegistrationService,
) {

  @PostMapping("/register")
  suspend fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse =
    registrationService.validateRequestAndRegister(registrationRequest)

  @ExceptionHandler
  suspend fun handleUsernameAlreadyTaken(e: UsernameAlreadyTakenException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
      title = UsernameAlreadyTakenException::class.simpleName
      detail = e.message
    }
}
