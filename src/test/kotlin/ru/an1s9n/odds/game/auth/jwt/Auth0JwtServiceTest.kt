package ru.an1s9n.odds.game.auth.jwt

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.BadCredentialsException
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class Auth0JwtServiceTest {

  private val jwtService: JwtService = Auth0JwtService("test-secret")

  @Test
  internal fun `ensure inValid returns true for valid token and extractIdFrom works correctly`() {
    val id = UUID.randomUUID()
    val token = jwtService.createTokenWith(id)
    assertTrue(jwtService.isValid(token))
    val decodedId = jwtService.extractIdFrom(token)
    assertEquals(id, decodedId)
  }

  @Test
  internal fun `ensure isValid returns false for token with invalid signature`() {
    val invalidToken = jwtService.createTokenWith(UUID.randomUUID()) + "foo"
    assertFalse(jwtService.isValid(invalidToken))
  }

  @Test
  internal fun `ensure isValid returns true for valid token with invalid content and extractIdFrom throws BadCredentialsException`() {
    val validTokenWithInvalidContent = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6ImJhciJ9.aJ48ZDK90vEsdynWMa9alSUwpcEpLsEJ0EK6pQtiWCA"
    assertTrue(jwtService.isValid(validTokenWithInvalidContent))
    assertThrows<BadCredentialsException> { jwtService.extractIdFrom(validTokenWithInvalidContent) }
  }
}
