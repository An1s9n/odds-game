package ru.an1s9n.odds.game.transaction.service

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.TestConstructor
import ru.an1s9n.odds.game.player.repository.Player
import ru.an1s9n.odds.game.player.repository.PlayerRepository
import ru.an1s9n.odds.game.transaction.dto.TransactionType
import ru.an1s9n.odds.game.transaction.repository.Transaction
import ru.an1s9n.odds.game.transaction.repository.TransactionRepository
import ru.an1s9n.odds.game.util.nowUtc
import kotlin.test.assertEquals

@DataR2dbcTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultTransactionServiceTest(
  private val defaultTransactionService: DefaultTransactionService,
  private val transactionRepository: TransactionRepository,
  private val playerRepository: PlayerRepository,
) {

  @AfterEach
  internal fun cleanupDb() {
    listOf(transactionRepository, playerRepository).forEach { repo -> runBlocking { repo.deleteAll() } }
  }

  @Test
  internal fun `test add, ensure new transaction added`() {
    runBlocking {
      val testPlayer = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val addedTransaction = defaultTransactionService.add(Transaction(playerId = testPlayer.id!!, timestampUtc = nowUtc(), amountCents = 10, type = TransactionType.PRIZE))

      with(addedTransaction) {
        assertEquals(testPlayer.id, playerId)
        assertEquals(10, amountCents)
        assertEquals(TransactionType.PRIZE, type)
      }
    }
  }

  @Test
  internal fun `ensure findAllByPlayerFreshFirst fetches transactions correctly with correct pagination`() {
    runBlocking {
      val an1s9n = playerRepository.save(Player(username = "An1s9n", firstName = "Pavel", lastName = "Anisimov", walletCents = 0))
      val fuzzPlayer = playerRepository.save(Player(username = "FuzzPlayer", firstName = "Fuzz", lastName = "Ololoev", walletCents = 0))
      transactionRepository.save(Transaction(playerId = an1s9n.id!!, timestampUtc = nowUtc().minusDays(1), amountCents = 10, type = TransactionType.PRIZE))
      transactionRepository.save(Transaction(playerId = an1s9n.id!!, timestampUtc = nowUtc(), amountCents = 11, type = TransactionType.PRIZE))
      transactionRepository.save(Transaction(playerId = an1s9n.id!!, timestampUtc = nowUtc().minusYears(1), amountCents = 12, type = TransactionType.PRIZE))
      transactionRepository.save(Transaction(playerId = fuzzPlayer.id!!, timestampUtc = nowUtc(), amountCents = 13, type = TransactionType.PRIZE))

      with(defaultTransactionService.findAllByPlayerFreshFirst(an1s9n, 1, 2).toList()) {
        assertEquals(2, size)
        assertEquals(11, get(0).amountCents)
        assertEquals(10, get(1).amountCents)
      }

      with(defaultTransactionService.findAllByPlayerFreshFirst(an1s9n, 2, 2).toList()) {
        assertEquals(1, size)
        assertEquals(12, get(0).amountCents)
      }
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  @ComponentScan(basePackageClasses = [TransactionService::class])
  internal class DefaultTransactionServiceTestConfig
}
