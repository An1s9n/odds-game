package ru.an1s9n.odds.game

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import kotlin.test.assertNotNull

@SpringBootTest
internal class OddsGameAppTest(
  private val appCtx: ApplicationContext,
) {

  @Test
  internal fun ensureContextLoads() {
    assertNotNull(appCtx)
  }
}
