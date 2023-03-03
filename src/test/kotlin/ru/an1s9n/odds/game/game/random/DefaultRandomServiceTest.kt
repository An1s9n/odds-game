package ru.an1s9n.odds.game.game.random

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import ru.an1s9n.odds.game.config.properties.GameRangeProperties
import kotlin.test.assertTrue

@SpringJUnitConfig
@TestPropertySource(properties = ["app.game.range.left-inclusive=2", "app.game.range.right-inclusive=7"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultRandomServiceTest(
  private val randomService: RandomService,
) {

  @Test
  internal fun `ensure generated prize number does not fall out of property-set range bounds`() {
    repeat(1_000) {
      val random = randomService.generatePrizeNumber()
      assertTrue(random in 2..7)
    }
  }

  @Configuration
  @EnableConfigurationProperties(GameRangeProperties::class)
  @ComponentScan(basePackageClasses = [RandomService::class])
  internal class DefaultRandomServiceTestConfig
}
