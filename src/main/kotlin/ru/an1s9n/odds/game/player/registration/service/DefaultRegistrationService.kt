package ru.an1s9n.odds.game.player.registration.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.odds.game.auth.BEARER_PREFIX
import ru.an1s9n.odds.game.auth.jwt.JwtService
import ru.an1s9n.odds.game.config.properties.GameProperties
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.dto.TransactionType
import ru.an1s9n.odds.game.transaction.repository.Transaction
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.nowUtc

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
  override suspend fun validateRequestAndRegister(registrationRequest: RegistrationRequest): RegistrationResponse {
    log.info("incoming registration request: $registrationRequest")
    validate(registrationRequest)
      ?.let { violations -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, violations.joinToString()) }

    val player = playerService.add(
      Player(
        username = registrationRequest.username.trim(),
        firstName = registrationRequest.firstName.trim(),
        lastName = registrationRequest.lastName.trim(),
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

    return RegistrationResponse(player, BEARER_PREFIX + jwtService.createTokenWith(player.id))
      .also { log.info("new player successfully registered: $it") }
  }

  private fun validate(registrationRequest: RegistrationRequest): List<String>? =
    buildList {
      if (registrationRequest.username.isBlank()) {
        add("username can not be blank")
      }
      if (registrationRequest.firstName.isBlank()) {
        add("firstName can not be blank")
      }
      if (registrationRequest.lastName.isBlank()) {
        add("lastName can not be blank")
      }
    }.ifEmpty { null }
      ?.also { log.info("registration request $registrationRequest is invalid: $it") }
}
