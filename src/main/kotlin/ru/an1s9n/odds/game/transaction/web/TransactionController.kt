package ru.an1s9n.odds.game.transaction.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ProblemDetail
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.an1s9n.odds.game.player.model.Player
import ru.an1s9n.odds.game.player.registration.response.RegistrationResponse
import ru.an1s9n.odds.game.transaction.model.Transaction
import ru.an1s9n.odds.game.transaction.service.TransactionService

@RestController
@RequestMapping("/transaction")
@Tag(name = "transaction")
@Validated
class TransactionController(
  private val transactionService: TransactionService,
) {

  @GetMapping("/my")
  @SecurityRequirement(name = "JWT Authorization")
  @Operation(
    summary = "get transactions of currently authenticated player",
    responses = [
      ApiResponse(responseCode = "200", description = "ok", content = [Content(schema = Schema(implementation = RegistrationResponse::class), mediaType = "application/json")]),
      ApiResponse(responseCode = "400", description = "invalid request", content = [Content(schema = Schema(implementation = ProblemDetail::class), mediaType = "application/problem+json")]),
      ApiResponse(responseCode = "401", description = "invalid token", content = [Content(schema = Schema(implementation = ProblemDetail::class), mediaType = "application/problem+json")]),
    ],
  )
  fun my(
    @Parameter(hidden = true) player: Player,
    @Min(1) @RequestParam(name = "page", defaultValue = "1") page: Int,
    @Min(1) @Max(100) @RequestParam(name = "perPage", defaultValue = "20") perPage: Int,
  ): Flow<Transaction> = transactionService.findAllByPlayerFreshFirst(player, page, perPage)
}
