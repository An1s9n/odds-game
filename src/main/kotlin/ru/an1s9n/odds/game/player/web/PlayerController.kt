package ru.an1s9n.odds.game.player.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player

@RestController
@RequestMapping("/player")
class PlayerController {

  @GetMapping("/me")
  suspend fun me(player: Player): Player = player
}
