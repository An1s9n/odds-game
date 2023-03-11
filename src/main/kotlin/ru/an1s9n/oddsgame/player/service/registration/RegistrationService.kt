package ru.an1s9n.oddsgame.player.service.registration

import ru.an1s9n.oddsgame.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationResponseDto

interface RegistrationService {

  suspend fun register(registrationRequestDto: RegistrationRequestDto): RegistrationResponseDto
}
