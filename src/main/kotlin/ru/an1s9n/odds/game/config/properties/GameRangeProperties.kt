package ru.an1s9n.odds.game.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.game.range")
data class GameRangeProperties(
  val leftInclusive: Int,
  val rightInclusive: Int,
)
