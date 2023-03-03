package ru.an1s9n.odds.game.jwt

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

private const val TEST_SECRET = "test-secret"

internal class Auth0JwtServiceTest {

  private val jwtService: JwtService = Auth0JwtService(TEST_SECRET)

  @Test
  internal fun `create and decode token, ensure id not changed`() {
    val id = UUID.randomUUID()
    val token = jwtService.createTokenWith(id)
    val decodedId = jwtService.validateAndExtractIdFrom(token)
    assertEquals(id, decodedId)
  }

  @Test
  internal fun `ensure invalid token decoding returns null`() {
    val invalidToken = jwtService.createTokenWith(UUID.randomUUID()) + "foo"
    val decodedId = jwtService.validateAndExtractIdFrom(invalidToken)
    assertNull(decodedId)
  }
}
