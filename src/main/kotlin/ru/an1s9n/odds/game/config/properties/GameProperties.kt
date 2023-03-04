package ru.an1s9n.odds.game.config.properties

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties

private const val PROPERTIES_PREFIX = "app.game"

@ConfigurationProperties(PROPERTIES_PREFIX)
data class GameProperties(

  val registrationCredits: Long,

  val range: GameRange,

  val offsetToPrizeFun: Map<Int, String>,
) {

  private val log = LoggerFactory.getLogger(this.javaClass)

  init {
      log.info("initialized $PROPERTIES_PREFIX properties: $this")
  }
}
