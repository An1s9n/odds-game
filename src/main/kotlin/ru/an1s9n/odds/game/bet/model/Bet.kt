package ru.an1s9n.odds.game.bet.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import java.time.LocalDateTime
import java.util.UUID

data class Bet(

  @Id
  @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  val id: UUID? = null,

  @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  val playerId: UUID,

  @Schema(example = "1994-05-05T14:15:00.000Z")
  val timestampUtc: LocalDateTime,

  @Schema(example = "5")
  val betNumber: Int,

  @Schema(example = "1000")
  val betCents: Long,

  @Schema(example = "10")
  val prizeNumber: Int,

  @Schema(example = "0")
  val prizeCents: Long,
)
