package ru.an1s9n.odds.game.web.resolver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player

@RestController
@RequestMapping("/test")
internal class PlayerArgumentResolvingTestController {

  @GetMapping("/resolve-player")
  internal fun resolvePlayer(player: Player): Player = player
}
