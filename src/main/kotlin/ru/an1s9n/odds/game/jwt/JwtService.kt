package ru.an1s9n.odds.game.jwt

import java.util.UUID

interface JwtService {

  /**
   * @return JWT containing single id claim
   */
  fun createTokenWith(id: UUID): String

  /**
   * @return id contained in JWT if token is valid and null otherwise
   */
  fun validateAndExtractIdFrom(token: String): UUID?
}
