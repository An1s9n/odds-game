package ru.an1s9n.odds.game.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

private const val ID = "id"

private const val BEARER_PREFIX = "Bearer "

@Service
class Auth0JwtService(
  @Value("\${app.jwt.secret}") secret: String,
) : JwtService {

  private val algorithm = Algorithm.HMAC256(secret)

  private val verifier = JWT.require(algorithm).build()

  override fun createTokenWith(id: UUID): String =
    BEARER_PREFIX + JWT.create().withClaim(ID, id.toString()).sign(algorithm)

  override fun validateAndExtractIdFrom(token: String): UUID? {
    val sanitizedToken = token.substringAfter(BEARER_PREFIX)
    if (!isValid(sanitizedToken)) {
      return null
    }

    return try {
      UUID.fromString(JWT.decode(sanitizedToken).getClaim(ID).asString())
    } catch (ignored: JWTDecodeException) {
      null
    }
  }

  private fun isValid(sanitizedToken: String): Boolean =
    try {
      verifier.verify(sanitizedToken)
      true
    } catch (ignored: JWTVerificationException) {
      false
    }
}
