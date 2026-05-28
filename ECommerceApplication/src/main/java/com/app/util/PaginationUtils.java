package com.app.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.app.payloads.CategoryResponse;
import com.app.payloads.OrderResponse;
import com.app.payloads.ProductResponse;
import com.app.payloads.UserResponse;

public final class PaginationUtils {

	private PaginationUtils() {
	}

	public static Sort createSort(String sortBy, String sortOrder) {
		return sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
				: Sort.by(sortBy).descending();
	}

	public static Pageable createPageable(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		return PageRequest.of(pageNumber, pageSize, createSort(sortBy, sortOrder));
	}

	public static void populateProductResponse(ProductResponse response, Page<?> page) {
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLastPage(page.isLast());
	}

	public static void populateCategoryResponse(CategoryResponse response, Page<?> page) {
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLastPage(page.isLast());
	}

	public static void populateUserResponse(UserResponse response, Page<?> page) {
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLastPage(page.isLast());
	}

	public static void populateOrderResponse(OrderResponse response, Page<?> page) {
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalElements(page.getTotalElements());
		response.setTotalPages(page.getTotalPages());
		response.setLastPage(page.isLast());
	}

}
