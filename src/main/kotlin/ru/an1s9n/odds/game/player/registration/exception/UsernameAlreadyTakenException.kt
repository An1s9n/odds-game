package ru.an1s9n.odds.game.player.registration.exception

class UsernameAlreadyTakenException(
  username: String,
) : RuntimeException("username $username is already taken")
