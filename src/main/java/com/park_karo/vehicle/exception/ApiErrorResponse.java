package com.park_karo.vehicle.exception;

import java.time.OffsetDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "timestamp", "status", "error", "message", "path", "errors", "errorId", "debug" })
public class ApiErrorResponse {

	private OffsetDateTime timestamp;
	private int status;
	private String error;
	private String message;
	private String path;
	private Map<String, String> errors;
	private String errorId; // Unique ID for tracing
	private String debug; // Optional debug info (stack trace)

	// ------------------- Private Constructor -------------------
	private ApiErrorResponse(Builder builder) {
		this.timestamp = builder.timestamp;
		this.status = builder.status;
		this.error = builder.error;
		this.message = builder.message;
		this.path = builder.path;
		this.errors = builder.errors;
		this.errorId = builder.errorId;
		this.debug = builder.debug;
	}

	// ------------------- Getters -------------------
	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public int getStatus() {
		return status;
	}

	public String getError() {
		return error;
	}

	public String getMessage() {
		return message;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public String getErrorId() {
		return errorId;
	}

	public String getDebug() {
		return debug;
	}

	// ------------------- Builder -------------------
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private OffsetDateTime timestamp = OffsetDateTime.now();
		private int status;
		private String error;
		private String message;
		private String path;
		private Map<String, String> errors;
		private String errorId;
		private String debug;

		public Builder timestamp(OffsetDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder status(int status) {
			this.status = status;
			return this;
		}

		public Builder error(String error) {
			this.error = error;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder errors(Map<String, String> errors) {
			this.errors = errors;
			return this;
		}

		public Builder errorId(String errorId) {
			this.errorId = errorId;
			return this;
		}

		public Builder debug(String debug) {
			this.debug = debug;
			return this;
		}

		public ApiErrorResponse build() {
			return new ApiErrorResponse(this);
		}
	}
}
