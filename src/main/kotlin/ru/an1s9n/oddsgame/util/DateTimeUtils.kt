package ru.an1s9n.oddsgame.util

import java.time.LocalDateTime
import java.time.ZoneOffset

fun nowUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
