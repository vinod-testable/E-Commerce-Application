package com.app.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.app.payloads.ProductResponse;

class PaginationUtilsTest {

	@Test
	void createSortAscending() {
		Sort sort = PaginationUtils.createSort("productId", "asc");
		assertTrue(sort.getOrderFor("productId").isAscending());
	}

	@Test
	void createSortDescending() {
		Sort sort = PaginationUtils.createSort("productId", "desc");
		assertTrue(sort.getOrderFor("productId").isDescending());
	}

	@Test
	void populateProductResponseSetsPaginationFields() {
		Page<String> page = new PageImpl<>(java.util.List.of("a"), PageRequest.of(1, 5), 12);
		ProductResponse response = new ProductResponse();

		PaginationUtils.populateProductResponse(response, page);

		assertEquals(1, response.getPageNumber());
		assertEquals(5, response.getPageSize());
		assertEquals(12, response.getTotalElements());
		assertEquals(3, response.getTotalPages());
		assertFalse(response.isLastPage());
	}

}
