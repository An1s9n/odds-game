package ru.an1s9n.odds.game.game.random

interface RandomService {

  fun getRandomFrom(range: IntRange): Int
}
