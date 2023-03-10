package ru.an1s9n.oddsgame.transaction.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import ru.an1s9n.oddsgame.player.repository.Player
import ru.an1s9n.oddsgame.transaction.dto.TransactionDto
import ru.an1s9n.oddsgame.transaction.repository.Transaction
import ru.an1s9n.oddsgame.transaction.repository.TransactionRepository

@Service
class DefaultTransactionService(
  private val transactionRepository: TransactionRepository,
) : TransactionService {

  override suspend fun add(transaction: Transaction): TransactionDto =
    transactionRepository.save(transaction).toDto()

  override fun findAllByPlayerFreshFirst(player: Player, page: Int, perPage: Int): Flow<TransactionDto> =
    transactionRepository
      .findAllByPlayerIdOrderByTimestampUtcDesc(player.id!!, PageRequest.of(page - 1, perPage))
      .map { it.toDto() }

  private fun Transaction.toDto() = TransactionDto(
    id = id,
    playerId = playerId,
    timestampUtc = timestampUtc,
    amountCents = amountCents,
    type = type,
  )
}
