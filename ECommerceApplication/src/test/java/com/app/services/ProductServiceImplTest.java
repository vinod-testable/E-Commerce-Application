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
import com.app.payloads.ProductDTO;
import com.app.payloads.ProductResponse;
import com.app.repositories.CartRepo;
import com.app.repositories.CategoryRepo;
import com.app.repositories.ProductRepo;
import com.app.util.DtoMappingHelper;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

	@Mock
	private ProductRepo productRepo;

	@Mock
	private CategoryRepo categoryRepo;

	@Mock
	private CartRepo cartRepo;

	@Mock
	private CartService cartService;

	@Mock
	private FileService fileService;

	@Mock
	private ModelMapper modelMapper;

	@Mock
	private DtoMappingHelper dtoMappingHelper;

	@InjectMocks
	private ProductServiceImpl productService;

	private Category category;
	private Product product;

	@BeforeEach
	void setUp() {
		category = new Category(1L, "Electronics", new ArrayList<>());
		product = new Product();
		product.setProductId(1L);
		product.setProductName("Phone");
		product.setDescription("Smart phone device");
		product.setPrice(100.0);
		product.setDiscount(10.0);
		product.setQuantity(5);
		category.setProducts(new ArrayList<>());
	}

	@Test
	void addProductSucceeds() {
		when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
		when(productRepo.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
		when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(new ProductDTO());

		ProductDTO result = productService.addProduct(1L, product);

		assertNotNull(result);
		verify(productRepo).save(any(Product.class));
	}

	@Test
	void addProductThrowsWhenDuplicate() {
		category.setProducts(List.of(product));
		when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

		assertThrows(APIException.class, () -> productService.addProduct(1L, product));
	}

	@Test
	void addProductThrowsWhenCategoryNotFound() {
		when(categoryRepo.findById(1L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> productService.addProduct(1L, product));
	}

	@Test
	void getAllProductsReturnsPagedResults() {
		Page<Product> page = new PageImpl<>(List.of(product));
		when(productRepo.findAll(any(Pageable.class))).thenReturn(page);
		when(modelMapper.map(product, ProductDTO.class)).thenReturn(new ProductDTO());

		ProductResponse response = productService.getAllProducts(0, 10, "productId", "asc");

		assertEquals(1, response.getContent().size());
	}

	@Test
	void searchProductByKeywordThrowsWhenEmpty() {
		Page<Product> page = new PageImpl<>(List.of());
		when(productRepo.findByProductNameLike(any(), any(Pageable.class))).thenReturn(page);

		assertThrows(APIException.class,
				() -> productService.searchProductByKeyword("missing", 0, 10, "productId", "asc"));
	}

	@Test
	void deleteProductRemovesFromCarts() {
		when(productRepo.findById(1L)).thenReturn(Optional.of(product));
		when(cartRepo.findCartsByProductId(1L)).thenReturn(List.of());

		String result = productService.deleteProduct(1L);

		verify(productRepo).delete(product);
		assertEquals("Product with productId: 1 deleted successfully !!!", result);
	}

}
