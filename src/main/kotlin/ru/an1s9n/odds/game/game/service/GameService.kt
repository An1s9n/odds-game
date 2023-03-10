package ru.an1s9n.odds.game.game.service

import ru.an1s9n.odds.game.bet.dto.BetDto
import ru.an1s9n.odds.game.game.dto.PlayRequestDto
import ru.an1s9n.odds.game.player.repository.Player

interface GameService {

  suspend fun validateRequestAndPlay(player: Player, playRequestDto: PlayRequestDto): BetDto
}
