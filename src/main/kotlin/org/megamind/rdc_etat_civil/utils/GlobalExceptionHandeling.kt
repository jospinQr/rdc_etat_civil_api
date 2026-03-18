package org.megamind.rdc_etat_civil.utils

import jakarta.persistence.EntityNotFoundException
import org.apache.coyote.BadRequestException

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

// 🔹 Modèle d'erreur uniforme
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // ⚠️ Conflits d’intégrité (doublons, clé étrangère, etc.)
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleConflict(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        logger.error("Conflit de données", e)
        val response = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = "Conflit de données : ${e.mostSpecificCause.message}"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    // ⚠️ Entité non trouvée
    @ExceptionHandler(EntityNotFoundException::class)
    fun handleNotFound(e: EntityNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Entité non trouvée : ${e.message}")
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = e.message
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
 // ⚠️ Entité non trouvée
    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ErrorResponse> {
        logger.warn("Mauvaise requette : ${e.message}")
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad request",
            message = e.message
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // ⚠️ Erreurs de validation (DTOs annotés avec @Valid)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "Invalid") }
        logger.warn("Erreur de validation : $errors")
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Error",
            message = errors.entries.joinToString { "${it.key}: ${it.value}" }
        )
        return ResponseEntity.badRequest().body(response)
    }

    // ⚠️ Erreurs de logique métier (IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("Argument illégal : ${e.message}")
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "${e.message}",
            message = e.message
        )
        return ResponseEntity.badRequest().body(response)
    }

    // ⚠️ Erreurs d’état illégal (IllegalStateException)
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.error("État illégal : ${e.message}")
        val response = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = "Illegal State",
            message = e.message
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    // ⚠️ Erreurs inattendues (catch-all)
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Erreur inattendue", e)
        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "Une erreur inattendue s'est produite"
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}