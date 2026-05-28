package com.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.CartDTO;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.ProductRepo;
import com.app.util.DtoMappingHelper;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

	@Mock
	private CartRepo cartRepo;

	@Mock
	private ProductRepo productRepo;

	@Mock
	private CartItemRepo cartItemRepo;

	@Mock
	private ModelMapper modelMapper;

	@Mock
	private DtoMappingHelper dtoMappingHelper;

	@InjectMocks
	private CartServiceImpl cartService;

	private Cart cart;
	private Product product;

	@BeforeEach
	void setUp() {
		cart = new Cart();
		cart.setCartId(1L);
		cart.setCartItems(new ArrayList<>());
		cart.setTotalPrice(0.0);

		product = new Product();
		product.setProductId(10L);
		product.setProductName("Laptop");
		product.setQuantity(5);
		product.setDiscount(5.0);
		product.setSpecialPrice(950.0);
	}

	@Test
	void addProductToCartSucceeds() {
		when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
		when(productRepo.findById(10L)).thenReturn(Optional.of(product));
		when(cartItemRepo.findCartItemByProductIdAndCartId(1L, 10L)).thenReturn(null);
		when(dtoMappingHelper.toCartDTO(cart)).thenReturn(new CartDTO());

		CartDTO result = cartService.addProductToCart(1L, 10L, 2);

		assertNotNull(result);
		verify(cartItemRepo).save(any(CartItem.class));
	}

	@Test
	void addProductToCartThrowsWhenProductAlreadyInCart() {
		when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
		when(productRepo.findById(10L)).thenReturn(Optional.of(product));
		when(cartItemRepo.findCartItemByProductIdAndCartId(1L, 10L)).thenReturn(new CartItem());

		assertThrows(APIException.class, () -> cartService.addProductToCart(1L, 10L, 1));
	}

	@Test
	void addProductToCartThrowsWhenOutOfStock() {
		product.setQuantity(0);
		when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
		when(productRepo.findById(10L)).thenReturn(Optional.of(product));
		when(cartItemRepo.findCartItemByProductIdAndCartId(1L, 10L)).thenReturn(null);

		assertThrows(APIException.class, () -> cartService.addProductToCart(1L, 10L, 1));
	}

	@Test
	void getAllCartsThrowsWhenEmpty() {
		when(cartRepo.findAll()).thenReturn(List.of());

		assertThrows(APIException.class, () -> cartService.getAllCarts());
	}

	@Test
	void getCartThrowsWhenNotFound() {
		when(cartRepo.findCartByEmailAndCartId("user@test.com", 1L)).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () -> cartService.getCart("user@test.com", 1L));
	}

	@Test
	void deleteProductFromCartSucceeds() {
		CartItem cartItem = new CartItem();
		cartItem.setProduct(product);
		cartItem.setProductPrice(100.0);
		cartItem.setQuantity(1);

		when(cartRepo.findById(1L)).thenReturn(Optional.of(cart));
		when(cartItemRepo.findCartItemByProductIdAndCartId(1L, 10L)).thenReturn(cartItem);

		String result = cartService.deleteProductFromCart(1L, 10L);

		verify(cartItemRepo).deleteCartItemByProductIdAndCartId(1L, 10L);
		assertEquals("Product Laptop removed from the cart !!!", result);
	}

}
