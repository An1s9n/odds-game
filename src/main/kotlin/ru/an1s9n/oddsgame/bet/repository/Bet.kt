package ru.an1s9n.oddsgame.bet.repository

import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class Bet(

  @Id
  val id: UUID? = null,

  val playerId: UUID,

  val timestampUtc: LocalDateTime,

  val betNumber: Int,

  val betCents: Long,

  val prizeNumber: Int,

  val prizeCents: Long,
)
