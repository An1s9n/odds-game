package ru.an1s9n.odds.game.game.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.bet.model.Bet
import ru.an1s9n.odds.game.game.model.request.PlayRequest
import ru.an1s9n.odds.game.game.service.GameService
import ru.an1s9n.odds.game.player.model.Player

@RestController
@RequestMapping("/game")
class GameController(
  private val gameService: GameService,
) {

  @PostMapping("/play")
  suspend fun play(player: Player, @RequestBody playRequest: PlayRequest): Bet =
    gameService.validateRequestAndPlay(player,  playRequest)
}
