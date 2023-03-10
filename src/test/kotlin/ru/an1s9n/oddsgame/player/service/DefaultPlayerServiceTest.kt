package ru.an1s9n.oddsgame.player.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.oddsgame.OddsGameApp
import ru.an1s9n.oddsgame.bet.repository.Bet
import ru.an1s9n.oddsgame.bet.repository.BetRepository
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.repository.PlayerRepository
import ru.an1s9n.oddsgame.util.nowUtc
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
  internal fun `test getById when player exists, ensure player fetched`() {
    runBlocking {
      val testPlayer = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val fetchedPlayer = defaultPlayerService.getById(testPlayer.id!!)

      assertNotNull(fetchedPlayer)
      assertEquals("An1s9n", fetchedPlayer.username)
    }
  }

  @Test
  internal fun `test getById when player does not exist, ensure null returned`() {
    runBlocking {
      val fetchedPlayer = defaultPlayerService.getById(UUID.randomUUID())

      assertNull(fetchedPlayer)
    }
  }

  @Test
  internal fun `test add, ensure new player added`() {
    runBlocking {
      val newPlayer = defaultPlayerService.add(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))

      val fetchedPlayer = playerRepository.findById(newPlayer.id!!)
      assertNotNull(fetchedPlayer)
      assertEquals("An1s9n", fetchedPlayer.username)
    }
  }

  @Test
  internal fun `test add when player with such username exists, ensure bad request ResponseStatusException thrown`() {
    runBlocking {
      playerRepository.save(Player(username = "An1s9n", firstName = "Fedor", lastName = "Fedorov", walletCents = 0))

      val e = assertThrows<ResponseStatusException> {
        defaultPlayerService.add(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      }
      assertEquals(HttpStatus.BAD_REQUEST, e.statusCode)
      assertEquals("username An1s9n is already taken", e.reason)
    }
  }

  @Test
  internal fun `ensure addToWallet adds and subtracts specified sum to player's wallet`() {
    runBlocking {
      val testPlayer = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 15))

      defaultPlayerService.addToWallet(testPlayer, 5)

      assertEquals(20, playerRepository.findById(testPlayer.id!!)!!.walletCents)
    }
  }

  @Test
  internal fun `ensure addToWallet throws OptimisticLockingFailureException if player's version changed`() {
    runBlocking {
      val testPlayer = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 15))
      val testPlayerCopy = testPlayer.copy()

      defaultPlayerService.addToWallet(testPlayer, 2)
      assertThrows<OptimisticLockingFailureException> { defaultPlayerService.addToWallet(testPlayerCopy, 3) }

      assertEquals(17, playerRepository.findById(testPlayer.id!!)!!.walletCents)
    }
  }

  @Test
  internal fun `ensure getTopBySumPrize fetches players correctly with correct pagination`() {
    runBlocking {
      val an1s9n = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val fooMan = playerRepository.save(Player(username = "FooMan", firstName = "Foo", lastName = "Ivanov", walletCents = 0))
      val barMan = playerRepository.save(Player(username = "BarMan", firstName = "Bar", lastName = "Petrov", walletCents = 0))
      val looser = playerRepository.save(Player(username = "looser", firstName = "looser", lastName = "looserov", walletCents = 0))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 70))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 0))
      betRepository.save(Bet(playerId = an1s9n.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 68))
      betRepository.save(Bet(playerId = fooMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 1))
      betRepository.save(Bet(playerId = fooMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 80))
      betRepository.save(Bet(playerId = barMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 0))
      betRepository.save(Bet(playerId = barMan.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 270))
      betRepository.save(Bet(playerId = looser.id!!, timestampUtc = nowUtc(), betNumber = 0, betCents = 10, prizeNumber = 0, prizeCents = 0))

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
  @ComponentScan(
    basePackageClasses = [OddsGameApp::class],
    useDefaultFilters = false,
    includeFilters = [
      ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [PlayerService::class],
      ),
    ],
  )
  internal class DefaultPlayerServiceTestConfig
}
