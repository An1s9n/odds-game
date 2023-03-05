package ru.an1s9n.odds.game.player.registration.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.an1s9n.odds.game.config.properties.GameProperties
import ru.an1s9n.odds.game.jwt.JwtService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.registration.request.RegistrationRequest
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.model.TransactionType
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.nowUtc
import ru.an1s9n.odds.game.web.exception.InvalidRequestException

@Service
class DefaultRegistrationService(
  gameProperties: GameProperties,
  private val playerService: PlayerService,
  private val transactionService: TransactionService,
  private val jwtService: JwtService,
) : RegistrationService {

  private val registrationCents = gameProperties.registrationCredits * 100

  @Transactional
  override suspend fun validateRequestAndRegister(registrationRequest: RegistrationRequest): RegistrationResponse {
    validate(registrationRequest)?.let { violations -> throw InvalidRequestException(violations) }

    val player = playerService.add(
      Player(
        username = registrationRequest.username.trim(),
        firstName = registrationRequest.firstName.trim(),
        lastName = registrationRequest.lastName.trim(),
        walletCents = registrationCents,
      )
    )
    transactionService.add(
      Transaction(
        playerId = player.id!!,
        timestampUtc = nowUtc(),
        amountCents = registrationCents,
        type = TransactionType.REGISTRATION,
      )
    )

    return RegistrationResponse(player, jwtService.createTokenWith(player.id))
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
}
