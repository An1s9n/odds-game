package ru.an1s9n.odds.game.game.service

import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.player.repository.Player

interface GameService {

  suspend fun validateRequestAndPlay(player: Player, playRequest: PlayRequest): BetDto
}
