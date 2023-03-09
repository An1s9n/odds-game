package ru.an1s9n.odds.game.auth.jwt

import java.util.UUID

interface JwtService {

  fun createTokenWith(id: UUID): String

  fun isValid(token: String): Boolean

  fun extractIdFrom(token: String): UUID
}
