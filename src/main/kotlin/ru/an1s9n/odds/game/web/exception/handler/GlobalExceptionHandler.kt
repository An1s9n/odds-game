package ru.an1s9n.odds.game.web.exception.handler

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.an1s9n.odds.game.web.exception.InvalidRequestException

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(InvalidRequestException::class, ConstraintViolationException::class)
  fun handleBadRequest(e: Exception): ProblemDetail = problemDetailFor(e, HttpStatus.BAD_REQUEST)

  private fun problemDetailFor(e: Exception, status: HttpStatus): ProblemDetail =
    ProblemDetail.forStatus(status).apply {
      title = e::class.simpleName
      detail = e.message
    }
}
