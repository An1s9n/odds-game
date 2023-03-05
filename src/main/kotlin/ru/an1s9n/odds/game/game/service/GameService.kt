package ru.an1s9n.odds.game.game.service

import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.player.model.Player

interface GameService {

  suspend fun validateRequestAndPlay(player: Player, playRequest: PlayRequest): Bet
}
