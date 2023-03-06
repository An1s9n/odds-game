package ru.an1s9n.odds.game.player.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestConstructor
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.util.nowUtc
import kotlin.test.assertEquals

@DataR2dbcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultPlayerServiceTest(
  private val defaultPlayerService: DefaultPlayerService,
  private val playerRepository: PlayerRepository,
  private val betRepository: BetRepository,
) {

  @AfterEach
  internal fun cleanupDb() {
    listOf(betRepository, playerRepository).forEach { repo -> runBlocking { repo.deleteAll() } }
  }

  @Test
  internal fun `ensure getTopBySumPrize fetches players correctly with correct pagination`() {
    runBlocking {
      val an1s9n = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val fooMan = playerRepository.save(Player(username = "FooMan", firstName = "Foo", lastName = "Ivanov", walletCents = 0))
      val barMan = playerRepository.save(Player(username = "BarMan", firstName = "Bar", lastName = "Petrov", walletCents = 0))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 70))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 0))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 68))
      betRepository.save(Bet(playerId = fooMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 1))
      betRepository.save(Bet(playerId = fooMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 80))
      betRepository.save(Bet(playerId = barMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 0))
      betRepository.save(Bet(playerId = barMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 270))

      with(defaultPlayerService.getTopBySumPrize(page = 1, perPage = 2).toList()) {
        assertEquals(2, size)
        with(get(0)) {
          assertEquals(barMan.username, username)
          assertEquals(270, sumPrizeCents)
        }
        with(get(1)) {
          assertEquals(an1s9n.username, username)
          assertEquals(138, sumPrizeCents)
        }
      }

      with(defaultPlayerService.getTopBySumPrize(page = 2, perPage = 2).toList()) {
        assertEquals(1, size)
        with(get(0)) {
          assertEquals(fooMan.username, username)
          assertEquals(81, sumPrizeCents)
        }
      }
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackageClasses = [PlayerService::class])
  internal class DefaultPlayerServiceTestConfig
}
