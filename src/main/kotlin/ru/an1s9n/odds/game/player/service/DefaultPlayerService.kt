package ru.an1s9n.odds.game.player.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import ru.an1s9n.odds.game.jwt.JwtService
import ru.an1s9n.odds.game.player.exception.UsernameAlreadyTakenException
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.model.request.RegistrationRequest
import ru.an1s9n.odds.game.player.model.response.RegistrationResponse
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.web.exception.InvalidRequestException
import java.util.UUID

@Service
class DefaultPlayerService(
  private val playerRepository: PlayerRepository,
  private val jwtService: JwtService,
  @Value("\${app.player.initial-balance-cents}") private val initialBalanceCents: Long,
) : PlayerService {

  override suspend fun getById(id: UUID): Player? = playerRepository.findById(id)

  override suspend fun register(registrationRequest: RegistrationRequest): RegistrationResponse {
    validate(registrationRequest).let { violations ->
      if (violations.isNotEmpty()) {
        throw InvalidRequestException(violations)
      }
    }

    val player = try {
      playerRepository.save(
        Player(
          username = registrationRequest.username.trim(),
          firstName = registrationRequest.firstName.trim(),
          lastName = registrationRequest.lastName.trim(),
          balanceCents = initialBalanceCents,
        )
      )
    } catch (e: DuplicateKeyException) {
      throw UsernameAlreadyTakenException(registrationRequest.username.trim())
    }
    return RegistrationResponse(player, jwtService.createTokenWith(player.id!!))
  }

  private fun validate(registrationRequest: RegistrationRequest): List<String> =
    buildList {
      if (registrationRequest.username.isBlank()) add("username can not be blank")
      if (registrationRequest.firstName.isBlank()) add("firstName can not be blank")
      if (registrationRequest.lastName.isBlank()) add("lastName can not be blank")
    }
}
