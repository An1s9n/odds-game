package ru.an1s9n.oddsgame

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OddsGameApp

fun main(args: Array<String>) {
  runApplication<OddsGameApp>(*args)
}
