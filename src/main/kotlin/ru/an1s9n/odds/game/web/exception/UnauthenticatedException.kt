package ru.an1s9n.odds.game.web.exception

class UnauthenticatedException : RuntimeException("authentication token is not provided or invalid")
