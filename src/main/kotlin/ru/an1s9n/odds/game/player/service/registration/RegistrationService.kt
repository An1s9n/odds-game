package ru.an1s9n.odds.game.player.service.registration

import ru.an1s9n.odds.game.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.odds.game.player.dto.registration.RegistrationResponseDto

interface RegistrationService {

  suspend fun validateRequestAndRegister(registrationRequestDto: RegistrationRequestDto): RegistrationResponseDto
}
