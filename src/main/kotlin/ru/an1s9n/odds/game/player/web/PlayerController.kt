package ru.an1s9n.odds.game.player.web

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.player.model.request.RegistrationRequest
import ru.an1s9n.odds.game.player.model.response.RegistrationResponse

@RestController
@RequestMapping("/player")
class PlayerController(
  private val playerService: PlayerService,
) {

  @GetMapping("/me")
  suspend fun me(player: Player): Player = player

  @PostMapping("/register")
  suspend fun register(@RequestBody registrationRequest: RegistrationRequest): RegistrationResponse =
    playerService.register(registrationRequest)

  @ExceptionHandler
  suspend fun handleUsernameAlreadyTaken(e: UsernameAlreadyTakenException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
      title = UsernameAlreadyTakenException::class.simpleName
      detail = e.message
    }
}
