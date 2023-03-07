package ru.an1s9n.odds.game.web

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.util.nowUtc

@RestController
@Hidden
class HealthcheckController {

  private val startupUtc = nowUtc()

  @GetMapping("/healthcheck")
  fun healthcheck(): Map<String, Any> = mapOf("status" to "ok", "startupUtc" to startupUtc)
}
