package ru.an1s9n.odds.game.web.exception

class InvalidRequestException(
  violations: List<String>,
) : RuntimeException(violations.joinToString())
