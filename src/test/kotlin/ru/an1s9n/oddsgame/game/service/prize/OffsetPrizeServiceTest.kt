package ru.an1s9n.oddsgame.game.service.prize

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.TestConstructor
import ru.an1s9n.oddsgame.config.properties.GameProperties
import kotlin.test.assertEquals

@SpringBootTest(
  properties = [
    "app.game.offset-to-prize-fun.0=*7",
    "app.game.offset-to-prize-fun.1=*3",
    "app.game.offset-to-prize-fun.2=*1",
    "app.game.offset-to-prize-fun.3=/2",
    "app.game.offset-to-prize-fun.4=/4",
  ],
)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class OffsetPrizeServiceTest(
  private val offsetPrizeService: OffsetPrizeService,
) {

  @Test
  internal fun `ensure prize defined according to properties-set rules`() {
    assertEquals(700, offsetPrizeService.definePrizeCents(10, 10, 100))
    assertEquals(300, offsetPrizeService.definePrizeCents(11, 10, 100))
    assertEquals(300, offsetPrizeService.definePrizeCents(9, 10, 100))
    assertEquals(100, offsetPrizeService.definePrizeCents(12, 10, 100))
    assertEquals(100, offsetPrizeService.definePrizeCents(8, 10, 100))
    assertEquals(50, offsetPrizeService.definePrizeCents(13, 10, 100))
    assertEquals(50, offsetPrizeService.definePrizeCents(7, 10, 100))
    assertEquals(25, offsetPrizeService.definePrizeCents(14, 10, 100))
    assertEquals(25, offsetPrizeService.definePrizeCents(6, 10, 100))
    assertEquals(0, offsetPrizeService.definePrizeCents(15, 10, 100))
    assertEquals(0, offsetPrizeService.definePrizeCents(5, 10, 100))
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(GameProperties::class)
  @ComponentScan(basePackageClasses = [PrizeService::class])
  internal class OffsetPrizeServiceTestConfig
}
