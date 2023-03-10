package ru.an1s9n.odds.game.bet.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.TestConstructor
import ru.an1s9n.odds.game.OddsGameApp
import ru.an1s9n.odds.game.bet.repository.Bet
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.util.nowUtc
import kotlin.test.assertEquals

@DataR2dbcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultBetServiceTest(
  private val defaultBetService: DefaultBetService,
  private val betRepository: BetRepository,
  private val playerRepository: PlayerRepository,
) {

  @AfterEach
  internal fun cleanupDb() {
    listOf(betRepository, playerRepository).forEach { repo -> runBlocking { repo.deleteAll() } }
  }

  @Test
  internal fun `test add, ensure new bet added`() {
    runBlocking {
      val testPlayer = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val addedBet = defaultBetService.add(Bet(playerId = testPlayer.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 5, prizeCents = 20))

      with(addedBet) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(0, betNumber)
        assertEquals(10, betCents)
        assertEquals(5, prizeNumber)
        assertEquals(20, prizeCents)
      }
    }
  }

  @Test
  internal fun `ensure findAllByPlayerFreshFirst fetches bets correctly with correct pagination`() {
    runBlocking {
      val an1s9n = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val fuzzPlayer = playerRepository.save(Player(username = "FuzzPlayer", firstName = "Fuzz", lastName = "Ololoev", walletCents = 0))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc().minusDays(1), betNumber = 0, betCents = 10, prizeNumber = 1, prizeCents = 70))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 1, betCents = 11, prizeNumber = 2, prizeCents = 71))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc().minusYears(1), betNumber = 2, betCents = 12, prizeNumber = 3, prizeCents = 72))
      betRepository.save(Bet(playerId = fuzzPlayer.id!!, timestampUtc = nowUtc(), betNumber = 3, betCents = 13, prizeNumber = 4, prizeCents = 73))

      with(defaultBetService.findAllByPlayerFreshFirst(an1s9n, 1, 2).toList()) {
        assertEquals(2, size)
        assertEquals(71, get(0).prizeCents)
        assertEquals(70, get(1).prizeCents)
      }

      with(defaultBetService.findAllByPlayerFreshFirst(an1s9n, 2, 2).toList()) {
        assertEquals(1, size)
        assertEquals(72, get(0).prizeCents)
      }
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(
    basePackageClasses = [OddsGameApp::class],
    useDefaultFilters = false,
    includeFilters = [
      ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [BetService::class],
      ),
    ],
  )
  internal class DefaultBetServiceTestConfig
}
