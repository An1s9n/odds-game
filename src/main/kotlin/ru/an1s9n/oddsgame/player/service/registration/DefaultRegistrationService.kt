package ru.an1s9n.oddsgame.player.service.registration

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.oddsgame.auth.BEARER_PREFIX
import ru.an1s9n.oddsgame.auth.jwt.JwtService
import ru.an1s9n.oddsgame.config.properties.GameProperties
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationResponseDto
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.service.PlayerService
import ru.an1s9n.oddsgame.transaction.dto.TransactionType
import ru.an1s9n.oddsgame.transaction.repository.Transaction
import ru.an1s9n.oddsgame.transaction.service.TransactionService
import ru.an1s9n.oddsgame.util.nowUtc

@Service
class DefaultRegistrationService(
  gameProperties: GameProperties,
  private val playerService: PlayerService,
  private val transactionService: TransactionService,
  private val jwtService: JwtService,
) : RegistrationService {

  private val log = LoggerFactory.getLogger(this.javaClass)

  private val registrationCents = gameProperties.registrationCredits * 100

  @Transactional
  override suspend fun register(registrationRequestDto: RegistrationRequestDto): RegistrationResponseDto {
    log.info("incoming registration request: $registrationRequestDto")
    validate(registrationRequestDto)
      ?.let { violations -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, violations.joinToString()) }

    val player = playerService.add(
      Player(
        username = registrationRequestDto.username.trim(),
        firstName = registrationRequestDto.firstName.trim(),
        lastName = registrationRequestDto.lastName.trim(),
        walletCents = registrationCents,
      ),
    )
    transactionService.add(
      Transaction(
        playerId = player.id!!,
        timestampUtc = nowUtc(),
        amountCents = registrationCents,
        type = TransactionType.REGISTRATION,
      ),
    )

    return RegistrationResponseDto(player, BEARER_PREFIX + jwtService.createTokenWith(player.id))
      .also { log.info("new player successfully registered: $it") }
  }

  private fun validate(registrationRequestDto: RegistrationRequestDto): List<String>? =
    buildList {
      if (registrationRequestDto.username.isBlank()) {
        add("username can not be blank")
      }
      if (registrationRequestDto.firstName.isBlank()) {
        add("firstName can not be blank")
      }
      if (registrationRequestDto.lastName.isBlank()) {
        add("lastName can not be blank")
      }
    }.ifEmpty { null }
      ?.also { log.info("registration request $registrationRequestDto is invalid: $it") }
}
