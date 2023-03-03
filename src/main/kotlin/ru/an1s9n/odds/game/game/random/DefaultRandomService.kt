package ru.an1s9n.odds.game.game.random

import org.springframework.stereotype.Service

@Service
class DefaultRandomService : RandomService {

  override fun getRandomFrom(range: IntRange): Int = range.random()
}
