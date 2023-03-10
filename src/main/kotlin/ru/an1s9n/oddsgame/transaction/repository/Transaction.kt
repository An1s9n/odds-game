package ru.an1s9n.oddsgame.transaction.repository

import org.springframework.data.annotation.Id
import ru.an1s9n.oddsgame.transaction.dto.TransactionType
import java.time.LocalDateTime
import java.util.UUID

data class Transaction(

  @Id
  val id: UUID? = null,

  val playerId: UUID,

  val timestampUtc: LocalDateTime,

  val amountCents: Long,

  val type: TransactionType,
)
