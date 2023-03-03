package ru.an1s9n.odds.game.player.registration.service

import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse

interface RegistrationService {

  suspend fun register(registrationRequest: RegistrationRequest): RegistrationResponse
}
