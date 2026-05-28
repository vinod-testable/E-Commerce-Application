package com.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.app.entites.Category;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CategoryDTO;
import com.app.payloads.CategoryResponse;
import com.app.repositories.CategoryRepo;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

	@Mock
	private CategoryRepo categoryRepo;

	@Mock
	private ProductService productService;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	private Category category;

	@BeforeEach
	void setUp() {
		category = new Category(1L, "Electronics", new ArrayList<>());
	}

	@Test
	void createCategorySucceeds() {
		when(categoryRepo.findByCategoryName("Electronics")).thenReturn(null);
		when(categoryRepo.save(category)).thenReturn(category);
		when(modelMapper.map(category, CategoryDTO.class)).thenReturn(new CategoryDTO());

		CategoryDTO result = categoryService.createCategory(category);

		assertNotNull(result);
		verify(categoryRepo).save(category);
	}

	@Test
	void createCategoryThrowsWhenDuplicate() {
		when(categoryRepo.findByCategoryName("Electronics")).thenReturn(category);

		assertThrows(APIException.class, () -> categoryService.createCategory(category));
	}

	@Test
	void getCategoriesReturnsPagedResults() {
		Page<Category> page = new PageImpl<>(List.of(category));
		when(categoryRepo.findAll(any(Pageable.class))).thenReturn(page);
		when(modelMapper.map(category, CategoryDTO.class)).thenReturn(new CategoryDTO());

		CategoryResponse response = categoryService.getCategories(0, 10, "categoryId", "asc");

		assertEquals(1, response.getContent().size());
	}

	@Test
	void getCategoriesThrowsWhenEmpty() {
		Page<Category> page = new PageImpl<>(List.of());
		when(categoryRepo.findAll(any(Pageable.class))).thenReturn(page);

		assertThrows(APIException.class, () -> categoryService.getCategories(0, 10, "categoryId", "asc"));
	}

	@Test
	void updateCategorySucceeds() {
		when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
		when(categoryRepo.save(category)).thenReturn(category);
		when(modelMapper.map(category, CategoryDTO.class)).thenReturn(new CategoryDTO());

		CategoryDTO result = categoryService.updateCategory(category, 1L);

		assertNotNull(result);
		assertEquals(1L, category.getCategoryId());
	}

	@Test
	void updateCategoryThrowsWhenNotFound() {
		when(categoryRepo.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(category, 99L));
	}

	@Test
	void deleteCategoryRemovesProductsFirst() {
		Product product = new Product();
		product.setProductId(5L);
		category.setProducts(List.of(product));

		when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

		String result = categoryService.deleteCategory(1L);

		verify(productService).deleteProduct(5L);
		verify(categoryRepo).delete(category);
		assertEquals("Category with categoryId: 1 deleted successfully !!!", result);
	}

}
