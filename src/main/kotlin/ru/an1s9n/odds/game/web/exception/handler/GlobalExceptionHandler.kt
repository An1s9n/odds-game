package ru.an1s9n.odds.game.web.exception.handler

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.an1s9n.odds.game.web.exception.InvalidRequestException
import ru.an1s9n.odds.game.web.exception.UnauthenticatedException

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler
  fun handleInvalidRequest(e: InvalidRequestException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
      title = InvalidRequestException::class.simpleName
      detail = e.message
    }

  @ExceptionHandler
  fun handleUnauthenticated(e: UnauthenticatedException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED).apply {
      title = UnauthenticatedException::class.simpleName
      detail = e.message
    }
}
