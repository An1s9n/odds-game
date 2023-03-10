package ru.an1s9n.oddsgame.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import java.util.UUID

private const val ID_CLAIM = "id"

class Auth0JwtService(
  secret: String,
) : JwtService {

  private val log = LoggerFactory.getLogger(this.javaClass)

  private val algorithm = Algorithm.HMAC256(secret)

  private val verifier = JWT.require(algorithm).build()

  override fun createTokenWith(id: UUID): String = JWT.create().withClaim(ID_CLAIM, id.toString()).sign(algorithm)

  override fun isValid(token: String): Boolean =
    try {
      verifier.verify(token)
      true
    } catch (ignored: JWTVerificationException) {
      false
    }

  override fun extractIdFrom(token: String): UUID =
    try {
      UUID.fromString(JWT.decode(token).getClaim(ID_CLAIM).asString())
    } catch (e: Exception) {
      log.error("exception while extracting id from token $token: $e")
      throw BadCredentialsException("invalid token")
    }
}
