package com.financecontrol.exception;

import com.financecontrol.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(
			BusinessException ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({
			BadCredentialsException.class,
			AuthenticationException.class
	})
	public ResponseEntity<ApiErrorResponse> handleAuthentication(
			Exception ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password", request.getRequestURI());
	}

	@ExceptionHandler(CsvImportException.class)
	public ResponseEntity<ApiErrorResponse> handleCsvImport(
			CsvImportException ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler({
			MaxUploadSizeExceededException.class,
			MultipartException.class
	})
	public ResponseEntity<ApiErrorResponse> handleMultipart(
			Exception ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Invalid multipart upload: " + ex.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.orElse("Validation failed");
		return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler({
			IllegalArgumentException.class,
			MethodArgumentTypeMismatchException.class,
			HttpMessageNotReadableException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(
			Exception ex,
			HttpServletRequest request) {
		String message = ex instanceof MethodArgumentTypeMismatchException mismatch
				? "Invalid value for parameter '" + mismatch.getName() + "'"
				: ex.getMessage();
		return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
			Exception ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI());
	}

	private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String message, String path) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				path
		);
		return ResponseEntity.status(status).body(body);
	}
}
