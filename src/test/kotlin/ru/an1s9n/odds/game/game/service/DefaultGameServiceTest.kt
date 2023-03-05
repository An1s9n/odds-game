package ru.an1s9n.odds.game.game.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestConstructor
import ru.an1s9n.odds.game.bet.repository.BetRepository
import ru.an1s9n.odds.game.bet.service.BetService
import ru.an1s9n.odds.game.config.properties.GameProperties
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.prize.PrizeService
import ru.an1s9n.odds.game.game.range.GameRangeService
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.transaction.model.TransactionType
import ru.an1s9n.odds.game.transaction.repository.TransactionRepository
import ru.an1s9n.odds.game.transaction.service.TransactionService
import ru.an1s9n.odds.game.util.sum
import kotlin.test.assertEquals

@DataR2dbcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultGameServiceTest(
  private val defaultGameService: DefaultGameService,
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
      defaultGameService.validateRequestAndPlay(testPlayer, PlayRequest(betNumber = 130, betCredits = 2))
      defaultGameService.validateRequestAndPlay(testPlayer, PlayRequest(betNumber = 129, betCredits = 3))
      defaultGameService.validateRequestAndPlay(testPlayer, PlayRequest(betNumber = 128, betCredits = 6))

      assertEquals(100, playerRepository.findAll().first().walletCents)

      val allTransactions = transactionRepository.findAll()
      assertEquals(5, allTransactions.count())
      assertEquals(-1100, allTransactions.filter { it.type == TransactionType.BET }.map { it.amountCents }.reduce(sum))
      assertEquals(700, allTransactions.filter { it.type == TransactionType.PRIZE }.map { it.amountCents }.reduce(sum))

      val allBets = betRepository.findAll()
      assertEquals(3, allBets.count())
      with(allBets.first { it.betNumber == 130 }) {
        assertEquals(testPlayer.id!!, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(200, betCents)
        assertEquals(400, prizeCents)
      }
      with(allBets.first { it.betNumber == 129 }) {
        assertEquals(testPlayer.id!!, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(300, betCents)
        assertEquals(300, prizeCents)
      }
      with(allBets.first { it.betNumber == 128 }) {
        assertEquals(testPlayer.id!!, playerId)
        assertEquals(130, prizeNumber)
        assertEquals(600, betCents)
        assertEquals(0, prizeCents)
      }
    }
  }

  private suspend fun persistTestPlayer(): Player =
    playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 500))

  @TestConfiguration(proxyBeanMethods = false)
  @EnableConfigurationProperties(GameProperties::class)
  @ComponentScan(basePackageClasses = [
    GameService::class,
    PlayerService::class,
    TransactionService::class,
    BetService::class,
  ])
  internal class DefaultGameServiceTestConfig
}
