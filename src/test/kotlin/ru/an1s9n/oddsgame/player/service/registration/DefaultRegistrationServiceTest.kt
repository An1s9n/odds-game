package ru.an1s9n.oddsgame.player.service.registration

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import kotlinx.coroutines.flow.first
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
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestConstructor
import org.springframework.web.server.ResponseStatusException
import ru.an1s9n.oddsgame.OddsGameApp
import ru.an1s9n.oddsgame.auth.jwt.JwtService
import ru.an1s9n.oddsgame.config.properties.GameProperties
import ru.an1s9n.oddsgame.player.dto.registration.RegistrationRequestDto
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.player.repository.PlayerRepository
import ru.an1s9n.oddsgame.player.service.PlayerService
import ru.an1s9n.oddsgame.transaction.dto.TransactionType
import ru.an1s9n.oddsgame.transaction.repository.TransactionRepository
import ru.an1s9n.oddsgame.transaction.service.TransactionService
import kotlin.test.assertEquals

@DataR2dbcTest(properties = ["app.game.registration-credits=27"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
internal class DefaultRegistrationServiceTest(
  private val defaultRegistrationService: DefaultRegistrationService,
  private val playerRepository: PlayerRepository,
  private val transactionRepository: TransactionRepository,
  @SpykBean private val spyTransactionService: TransactionService,
  @MockkBean private val mockJwtService: JwtService,
) {

  @BeforeEach
  internal fun initMocks() {
    every { mockJwtService.createTokenWith(any()) } returns "mock-token"
  }

  @AfterEach
  internal fun cleanupDb() {
    listOf(transactionRepository, playerRepository).forEach { repo -> runBlocking { repo.deleteAll() } }
  }

  @Test
  internal fun `test player registration, ensure new player with correct wallet and registration bonus accrual persisted`() {
    runBlocking {
      defaultRegistrationService.register(
        RegistrationRequestDto(
          username = "An1s9n",
          firstName = "Pavel",
          lastName = "Anisimov",
        ),
      )

      assertEquals(1, playerRepository.count())
      val addedPlayer = playerRepository.findAll().first()
      with(addedPlayer) {
        assertEquals("An1s9n", username)
        assertEquals("Pavel", firstName)
        assertEquals("Anisimov", lastName)
        assertEquals(2700, walletCents)
      }

      assertEquals(1, transactionRepository.count())
      with(transactionRepository.findAll().first()) {
        assertEquals(addedPlayer.id!!, playerId)
        assertEquals(2700, amountCents)
        assertEquals(TransactionType.REGISTRATION, type)
      }
    }
  }

  @Test
  internal fun `test player registration, ensure input trimmed`() {
    runBlocking {
      defaultRegistrationService.register(
        RegistrationRequestDto(
          username = "  An1s9n",
          firstName = "Pavel  ",
          lastName = "  Anisimov  ",
        ),
      )

      assertEquals(1, playerRepository.count())
      with(playerRepository.findAll().first()) {
        assertEquals("An1s9n", username)
        assertEquals("Pavel", firstName)
        assertEquals("Anisimov", lastName)
      }
    }
  }

  @Test
  internal fun `test player registration when input contains blank fields, ensure invalid request ResponseStatusException thrown`() {
    runBlocking {
      val e = assertThrows<ResponseStatusException> {
        defaultRegistrationService.register(
          RegistrationRequestDto(
            username = "An1s9n",
            firstName = "",
            lastName = " ",
          ),
        )
      }

      assertEquals("firstName can not be blank, lastName can not be blank", e.reason)
    }
  }

  @Test
  internal fun `test player registration when username is already taken, ensure bad request ResponseStatusException thrown`() {
    runBlocking {
      playerRepository.save(
        Player(
          username = "An1s9n",
          firstName = "Pavel",
          lastName = "Anisimov",
          walletCents = 0,
        ),
      )

      val e = assertThrows<ResponseStatusException> {
        defaultRegistrationService.register(
          RegistrationRequestDto(
            username = "An1s9n",
            firstName = "Pavel",
            lastName = "Anisimov",
          ),
        )
      }
      assertEquals(HttpStatus.BAD_REQUEST, e.statusCode)
      assertEquals("username An1s9n is already taken", e.reason)
    }
  }

  @Test
  internal fun `test player registration, ensure registration runs in transaction and player is not persisted when registration bonus accrual fails`() {
    every { runBlocking { spyTransactionService.add(any()) } } throws RuntimeException()

    runBlocking {
      assertThrows<RuntimeException> {
        defaultRegistrationService.register(
          RegistrationRequestDto(
            username = "An1s9n",
            firstName = "Pavel",
            lastName = "Anisimov",
          ),
        )
      }

      assertEquals(0, playerRepository.count())
      assertEquals(0, transactionRepository.count())
    }
  }

  @TestConfiguration(proxyBeanMethods = false)
  @EnableConfigurationProperties(GameProperties::class)
  @ComponentScan(
    basePackageClasses = [OddsGameApp::class],
    useDefaultFilters = false,
    includeFilters = [
      ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [
          PlayerService::class,
          TransactionService::class,
          RegistrationService::class,
        ],
      ),
    ],
  )
  internal class DefaultRegistrationServiceTestConfig
}
