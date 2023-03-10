package ru.an1s9n.oddsgame.web.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler
  fun handleConstraintViolation(e: ConstraintViolationException): ProblemDetail =
    ProblemDetail.forStatus(HttpStatus.BAD_REQUEST).apply {
      title = e::class.simpleName
      detail = e.message
    }
}
