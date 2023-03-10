package ru.an1s9n.odds.game.transaction.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class TransactionDto(

  @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  val id: UUID? = null,

  @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  val playerId: UUID,

  @Schema(example = "1994-05-05T14:15:00.000Z")
  val timestampUtc: LocalDateTime,

  @Schema(example = "-100")
  val amountCents: Long,

  val type: TransactionType,
)
