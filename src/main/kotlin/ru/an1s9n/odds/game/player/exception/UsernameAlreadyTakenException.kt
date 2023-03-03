package ru.an1s9n.odds.game.player.exception

class UsernameAlreadyTakenException(
  username: String,
) : RuntimeException("username $username is already taken")
