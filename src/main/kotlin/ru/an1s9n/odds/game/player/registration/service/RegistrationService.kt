package ru.an1s9n.odds.game.player.registration.service

import ru.an1s9n.odds.game.player.registration.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.web.exception.InvalidRequestException

interface RegistrationService {

  /**
   * @throws UsernameAlreadyTakenException if username is already taken by another player
   * @throws InvalidRequestException if registrationRequest is invalid
   */
  suspend fun validateRequestAndRegister(registrationRequest: RegistrationRequest): RegistrationResponse
}
