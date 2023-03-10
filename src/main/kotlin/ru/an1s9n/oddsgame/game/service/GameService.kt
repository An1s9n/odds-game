package ru.an1s9n.oddsgame.game.service

import ru.an1s9n.oddsgame.bet.dto.BetDto
import ru.an1s9n.oddsgame.game.dto.PlayRequestDto
import ru.an1s9n.oddsgame.player.repository.Player

interface GameService {

  suspend fun validateRequestAndPlay(player: Player, playRequestDto: PlayRequestDto): BetDto
}
