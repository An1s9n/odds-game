package ru.an1s9n.oddsgame.player.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.an1s9n.oddsgame.player.dto.TopPlayerDto
import java.util.UUID

interface PlayerRepository : CoroutineCrudRepository<Player, UUID> {

  @Query(
    """
    select username, first_name, last_name, wallet_cents, sum(prize_cents) sum_prize_cents
    from player p
        join bet b on p.id = b.player_id
    group by p.id
    having sum_prize_cents > 0
    order by sum_prize_cents desc
    limit :limit
    offset :offset
  """,
  )
  fun findTopByPrizeSum(limit: Int, offset: Int): Flow<TopPlayerDto>
}
