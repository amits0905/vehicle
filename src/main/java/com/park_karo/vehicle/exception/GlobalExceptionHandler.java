package com.park_karo.vehicle.exception;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// ================= 400 BAD REQUEST =================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
			WebRequest request) {

		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String message = error.getDefaultMessage();
			errors.put(fieldName, message);
		});

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error("Validation Failed").message("Request validation failed")
				.errors(errors).path(getRequestPath(request)).build();

		logger.warn("Validation error: {}", errors);
		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
			WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error("Bad Request").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Illegal argument: {}", ex.getMessage());
		return ResponseEntity.badRequest().body(errorResponse);
	}

	@ExceptionHandler(CustomExceptions.ValidationBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleValidationBusinessException(
			CustomExceptions.ValidationBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.BAD_REQUEST.value()).error("Validation Error").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Business validation error: {}", ex.getMessage());
		return ResponseEntity.badRequest().body(errorResponse);
	}

	// ================= 404 NOT FOUND =================

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
			WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.NOT_FOUND.value()).error("Endpoint Not Found")
				.message(String.format("Endpoint %s %s not found", ex.getHttpMethod(), ex.getRequestURL()))
				.path(getRequestPath(request)).build();

		logger.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(CustomExceptions.ResourceNotFoundBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
			CustomExceptions.ResourceNotFoundBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.NOT_FOUND.value()).error("Resource Not Found").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Resource not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	// ================= 409 CONFLICT =================

	@ExceptionHandler(CustomExceptions.ResourceAlreadyExistsBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExistsException(
			CustomExceptions.ResourceAlreadyExistsBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.CONFLICT.value()).error("Resource Already Exists").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Resource already exists: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex,
			WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.CONFLICT.value()).error("Data Integrity Violation")
				.message("Data integrity constraint violated").path(getRequestPath(request)).build();

		logger.error("Data integrity violation: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
	}

	// ================= 403 FORBIDDEN =================

	@ExceptionHandler(CustomExceptions.OperationNotPermittedBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleOperationNotPermittedException(
			CustomExceptions.OperationNotPermittedBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.FORBIDDEN.value()).error("Operation Not Permitted").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Operation not permitted: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	// ================= 429 TOO MANY REQUESTS =================

	@ExceptionHandler(CustomExceptions.RateLimitExceededBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleRateLimitExceededException(
			CustomExceptions.RateLimitExceededBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.TOO_MANY_REQUESTS.value()).error("Rate Limit Exceeded").message(ex.getMessage())
				.path(getRequestPath(request)).build();

		logger.warn("Rate limit exceeded: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
	}

	// ================= 500 DATABASE ERRORS =================

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {

		String rootCause = ex.getMostSpecificCause().getMessage();

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Database Error")
				.message("A database error occurred: " + rootCause).path(getRequestPath(request)).build();

		logger.error("Database error: {}", rootCause, ex);
		return ResponseEntity.internalServerError().body(errorResponse);
	}

	@ExceptionHandler(CustomExceptions.DatabaseConnectionBusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleDatabaseConnectionException(
			CustomExceptions.DatabaseConnectionBusinessException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.SERVICE_UNAVAILABLE.value()).error("Database Connection Error")
				.message(ex.getMessage()).path(getRequestPath(request)).build();

		logger.error("Database connection error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
	}

	// ================= 500 GENERAL ERRORS =================

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Internal Server Error")
				.message("An unexpected error occurred: " + ex.getMessage()).path(getRequestPath(request)).build();

		logger.error("Runtime exception: ", ex);
		return ResponseEntity.internalServerError().body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, WebRequest request) {

		ApiErrorResponse errorResponse = ApiErrorResponse.builder().timestamp(OffsetDateTime.now())
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).error("Internal Server Error")
				.message("An unexpected error occurred").path(getRequestPath(request)).build();

		logger.error("Unexpected exception: ", ex);
		return ResponseEntity.internalServerError().body(errorResponse);
	}

	// ================= HELPER METHODS =================

	private String getRequestPath(WebRequest request) {
		return request.getDescription(false).replace("uri=", "");
	}
}
