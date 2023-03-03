package ru.an1s9n.odds.game.player.service

import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.model.request.RegistrationRequest
import ru.an1s9n.odds.game.player.model.response.RegistrationResponse
import java.util.UUID

interface PlayerService {

  suspend fun getById(id: UUID): Player?

  suspend fun register(registrationRequest: RegistrationRequest): RegistrationResponse
}
