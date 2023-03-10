package ru.an1s9n.odds.game.game.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.TestConstructor
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.odds.game.OddsGameApp
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.bet.service.BetService
import ru.an1s9n.odds.game.config.properties.GameProperties
import ru.an1s9n.odds.game.game.dto.PlayRequestDto
import ru.an1s9n.odds.game.game.service.prize.PrizeService
import ru.an1s9n.odds.game.game.service.proxy.TransactionalProxyHelperGameService
import ru.an1s9n.odds.game.game.service.range.GameRangeService
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.dto.TransactionType
import ru.an1s9n.odds.game.transaction.repository.TransactionRepository
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.sum
import kotlin.test.assertEquals

@DataR2dbcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultGameServiceTest(
  @SpykBean private val spyDefaultGameService: DefaultGameService,
  private val transactionRepository: TransactionRepository,
  private val betRepository: BetRepository,
  private val playerRepository: PlayerRepository,
  @MockkBean private val mockGameRangeService: GameRangeService,
  @MockkBean private val mockPrizeService: PrizeService,
) {

  @BeforeEach
  internal fun initMocks() {
    every { mockGameRangeService.gameRange } returns 100..150
    every { mockGameRangeService.generatePrizeNumber() } returns 130
    every { mockPrizeService.definePrizeCents(eq(130), eq(130), any()) } answers { arg<Long>(2) * 2 }
    every { mockPrizeService.definePrizeCents(or(129, 131), eq(130), any()) } returnsArgument 2
    every { mockPrizeService.definePrizeCents(or(less(129), more(131)), eq(130), any()) } returns 0
  }

  @AfterEach
  internal fun cleanupDb() {
    listOf(transactionRepository, betRepository, playerRepository).forEach { repo -> runBlocking { repo.deleteAll() } }
  }

  @Test
  internal fun `test play, ensure transactions and bets created and user wallet updated correctly`() {
    runBlocking {
      val testPlayer = persistTestPlayer()
      spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 130, betCredits = 2))
      spyDefaultGameService.validateRequestAndPlay(playerRepository.findAll().first(), PlayRequestDto(betNumber = 129, betCredits = 3))
      spyDefaultGameService.validateRequestAndPlay(playerRepository.findAll().first(), PlayRequestDto(betNumber = 128, betCredits = 6))

      assertEquals(100, playerRepository.findAll().first().walletCents)

      val allTransactions = transactionRepository.findAll()
      assertEquals(5, allTransactions.count())
      assertEquals(-1100, allTransactions.filter { it.type == TransactionType.BET }.map { it.amountCents }.reduce(sum))
      assertEquals(700, allTransactions.filter { it.type == TransactionType.PRIZE }.map { it.amountCents }.reduce(sum))

      val allBets = betRepository.findAll()
      assertEquals(3, allBets.count())
      with(allBets.first { it.betNumber == 130 }) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(200, betCents)
        assertEquals(400, prizeCents)
      }
      with(allBets.first { it.betNumber == 129 }) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(300, betCents)
        assertEquals(300, prizeCents)
      }
      with(allBets.first { it.betNumber == 128 }) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(600, betCents)
        assertEquals(0, prizeCents)
      }
    }
  }

  @Test
  internal fun `test play when player has insufficient wallet, ensure invalid request ResponseStatusException thrown`() {
    runBlocking {
      val testPlayer = persistTestPlayer()

      val e = assertThrows<ResponseStatusException> {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 130, betCredits = 6))
      }
      assertEquals("insufficient wallet: required 600 cents, on wallet 500 cents", e.reason)
    }
  }

  @Test
  internal fun `test play when player bets on invalid number, ensure invalid request ResponseStatusException thrown`() {
    runBlocking {
      val testPlayer = persistTestPlayer()

      val e = assertThrows<ResponseStatusException> {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 200, betCredits = 3))
      }
      assertEquals("betNumber 200 is out of 100..150 game range", e.reason)
    }
  }

  @Test
  internal fun `test play when player bets with negative bet, ensure invalid request ResponseStatusException thrown`() {
    runBlocking {
      val testPlayer = persistTestPlayer()

      val e = assertThrows<ResponseStatusException> {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 125, betCredits = -3))
      }
      assertEquals("betCredits must be grater than 0", e.reason)
    }
  }

  @Test
  internal fun `test play when 2 games are concurrent, ensure retry is performed to handle optimistic locking exception`() {
    runBlocking {
      val testPlayer = persistTestPlayer()
      val firstGame = launch {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 129, betCredits = 1))
      }
      val secondGame = launch {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 128, betCredits = 2))
      }
      listOf(firstGame, secondGame).forEach { it.join() }

      assertEquals(300, playerRepository.findAll().first().walletCents)

      val allTransactions = transactionRepository.findAll()
      assertEquals(3, allTransactions.count())
      assertEquals(-300, allTransactions.filter { it.type == TransactionType.BET }.map { it.amountCents }.reduce(sum))
      assertEquals(100, allTransactions.filter { it.type == TransactionType.PRIZE }.map { it.amountCents }.reduce(sum))

      val allBets = betRepository.findAll()
      assertEquals(2, allBets.count())
      with(allBets.first { it.betNumber == 129 }) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(100, betCents)
        assertEquals(100, prizeCents)
      }
      with(allBets.first { it.betNumber == 128 }) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(200, betCents)
        assertEquals(0, prizeCents)
      }

      verify(exactly = 3) { runBlocking { spyDefaultGameService.validateRequestAndPlay(any(), any()) } }
    }
  }

  @Test
  internal fun `test play when 2 games are concurrent and first game exhausts player's wallet, ensure second game is not allowed`() {
    runBlocking {
      val testPlayer = persistTestPlayer()
      val firstGame = launch {
        spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 128, betCredits = 5))
      }
      val secondGame = launch {
        val e = assertThrows<ResponseStatusException> {
          spyDefaultGameService.validateRequestAndPlay(testPlayer, PlayRequestDto(betNumber = 130, betCredits = 1))
        }
        assertEquals("insufficient wallet: required 100 cents, on wallet 0 cents", e.reason)
      }
      listOf(firstGame, secondGame).forEach { it.join() }

      assertEquals(0, playerRepository.findAll().first().walletCents)

      assertEquals(1, transactionRepository.count())
      with(transactionRepository.findAll().first()) {
        assertEquals(TransactionType.BET, type)
        assertEquals(-500, amountCents)
      }

      assertEquals(1, betRepository.count())
      with(betRepository.findAll().first()) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(128, betNumber)
        assertEquals(130, prizeNumber)
        assertEquals(500, betCents)
        assertEquals(0, prizeCents)
      }

      verify(exactly = 3) { runBlocking { spyDefaultGameService.validateRequestAndPlay(any(), any()) } }
    }
  }

  private suspend fun persistTestPlayer(): Player =
    playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 500))

  @TestConfiguration(proxyBeanMethods = false)
  @EnableConfigurationProperties(GameProperties::class)
  @ComponentScan(
    basePackageClasses = [OddsGameApp::class],
    useDefaultFilters = false,
    includeFilters = [
      ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [
          GameService::class,
          PlayerService::class,
          TransactionService::class,
          BetService::class,
          TransactionalProxyHelperGameService::class,
        ],
      ),
    ],
  )
  internal class DefaultGameServiceTestConfig
}
