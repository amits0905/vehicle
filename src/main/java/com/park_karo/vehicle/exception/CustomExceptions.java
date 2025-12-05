package com.park_karo.vehicle.exception;

import java.io.Serial;

/**
 * Custom Business Exceptions for Vehicle Management System Note: Using
 * 'Business' suffix to avoid conflicts with Spring Boot exceptions
 */
public class CustomExceptions {

	/**
	 * Thrown when a requested resource is not found
	 */
	public static class ResourceNotFoundBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 1L;

		public ResourceNotFoundBusinessException(String message) {
			super(message);
		}

		public ResourceNotFoundBusinessException(String resource, String id) {
			super(String.format("%s with ID %s not found", resource, id));
		}

		public ResourceNotFoundBusinessException(String resource, String field, String value) {
			super(String.format("%s with %s = %s not found", resource, field, value));
		}
	}

	/**
	 * Thrown when trying to create a resource that already exists
	 */
	public static class ResourceAlreadyExistsBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 2L;

		public ResourceAlreadyExistsBusinessException(String message) {
			super(message);
		}

		public ResourceAlreadyExistsBusinessException(String resource, String id) {
			super(String.format("%s with ID %s already exists", resource, id));
		}
	}

	/**
	 * Thrown when database connection fails
	 */
	public static class DatabaseConnectionBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 3L;

		public DatabaseConnectionBusinessException(String message) {
			super(message);
		}

		public DatabaseConnectionBusinessException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Thrown when a business validation fails
	 */
	public static class ValidationBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 4L;

		public ValidationBusinessException(String message) {
			super(message);
		}

		public ValidationBusinessException(String field, String message) {
			super(String.format("Validation failed for %s: %s", field, message));
		}
	}

	/**
	 * Thrown when an operation is not permitted
	 */
	public static class OperationNotPermittedBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 5L;

		public OperationNotPermittedBusinessException(String message) {
			super(message);
		}

		public OperationNotPermittedBusinessException(String operation, String resource) {
			super(String.format("Operation '%s' not permitted on %s", operation, resource));
		}
	}

	/**
	 * Thrown when rate limit is exceeded
	 */
	public static class RateLimitExceededBusinessException extends RuntimeException {
		@Serial
		private static final long serialVersionUID = 6L;

		public RateLimitExceededBusinessException(String message) {
			super(message);
		}

		public RateLimitExceededBusinessException(String operation, int limit) {
			super(String.format("Rate limit exceeded for %s. Limit: %d requests per minute", operation, limit));
		}
	}
}