package ru.an1s9n.odds.game.player.top.web

import jakarta.validation.constraints.Positive
import kotlinx.coroutines.flow.Flow
import org.hibernate.validator.constraints.Range
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.service.PlayerService
import ru.an1s9n.odds.game.player.top.response.PlayerTopProjection

@RestController
@RequestMapping("/player/top")
@Validated
class PlayerTopController(
  private val playerService: PlayerService,
) {

  @GetMapping
  fun top(
    @Positive @RequestParam(defaultValue = "1") page: Int,
    @Range(min = 1, max = 100) @RequestParam(defaultValue = "20") perPage: Int,
  ): Flow<PlayerTopProjection> = playerService.getTopBySumPrize(page, perPage)
}
