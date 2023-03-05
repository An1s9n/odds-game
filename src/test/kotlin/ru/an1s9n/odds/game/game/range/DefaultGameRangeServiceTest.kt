package ru.an1s9n.odds.game.game.range

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.TestConstructor
import ru.an1s9n.odds.game.config.properties.GameProperties
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(properties = [
  "app.game.range.left-inclusive=2",
  "app.game.range.right-inclusive=7",
])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultGameRangeServiceTest(
  private val defaultRandomService: DefaultGameRangeService,
) {

  @Test
  internal fun gameRangeTest() {
    assertEquals(2..7, defaultRandomService.gameRange)
  }

  @Test
  internal fun `ensure generated prize number does not fall out of property-set range bounds`() {
    repeat(1_000) {
      val random = defaultRandomService.generatePrizeNumber()
      assertTrue(random in 2..7)
    }
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(GameProperties::class)
  @ComponentScan(basePackageClasses = [GameRangeService::class])
  internal class DefaultGameRangeServiceTestConfig
}
