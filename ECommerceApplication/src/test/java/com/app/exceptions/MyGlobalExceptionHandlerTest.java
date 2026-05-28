package com.app.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.app.payloads.APIResponse;

class MyGlobalExceptionHandlerTest {

	private final MyGlobalExceptionHandler handler = new MyGlobalExceptionHandler();

	@Test
	void handlesResourceNotFoundException() {
		ResponseEntity<APIResponse> response = handler
				.myResourceNotFoundException(new ResourceNotFoundException("User", "id", 1L));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertFalse(response.getBody().isStatus());
	}

	@Test
	void handlesApiException() {
		ResponseEntity<APIResponse> response = handler.myAPIException(new APIException("Invalid request"));

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Invalid request", response.getBody().getMessage());
	}

}
