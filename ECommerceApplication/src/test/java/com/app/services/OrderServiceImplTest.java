package com.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.Order;
import com.app.entites.Payment;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.OrderDTO;
import com.app.payloads.OrderResponse;
import com.app.repositories.CartItemRepo;
import com.app.repositories.CartRepo;
import com.app.repositories.OrderItemRepo;
import com.app.repositories.OrderRepo;
import com.app.repositories.PaymentRepo;
import com.app.repositories.UserRepo;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private UserRepo userRepo;

	@Mock
	private CartRepo cartRepo;

	@Mock
	private OrderRepo orderRepo;

	@Mock
	private PaymentRepo paymentRepo;

	@Mock
	private OrderItemRepo orderItemRepo;

	@Mock
	private CartItemRepo cartItemRepo;

	@Mock
	private UserService userService;

	@Mock
	private CartService cartService;

	@Mock
	private ModelMapper modelMapper;

	@InjectMocks
	private OrderServiceImpl orderService;

	private Cart cart;
	private CartItem cartItem;

	@BeforeEach
	void setUp() {
		Product product = new Product();
		product.setProductId(1L);
		product.setQuantity(10);

		cartItem = new CartItem();
		cartItem.setProduct(product);
		cartItem.setQuantity(2);
		cartItem.setDiscount(5.0);
		cartItem.setProductPrice(100.0);

		cart = new Cart();
		cart.setCartId(1L);
		cart.setTotalPrice(200.0);
		cart.setCartItems(new ArrayList<>(List.of(cartItem)));
	}

	@Test
	void placeOrderSucceeds() {
		Order order = new Order();
		order.setOrderId(1L);
		Payment payment = new Payment();

		when(cartRepo.findCartByEmailAndCartId("user@test.com", 1L)).thenReturn(cart);
		when(paymentRepo.save(any(Payment.class))).thenReturn(payment);
		when(orderRepo.save(any(Order.class))).thenReturn(order);
		when(orderItemRepo.saveAll(any())).thenReturn(List.of());
		when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

		OrderDTO result = orderService.placeOrder("user@test.com", 1L, "CARD");

		assertNotNull(result);
		verify(cartService).deleteProductFromCart(1L, 1L);
	}

	@Test
	void placeOrderThrowsWhenCartNotFound() {
		when(cartRepo.findCartByEmailAndCartId("user@test.com", 1L)).thenReturn(null);

		assertThrows(ResourceNotFoundException.class,
				() -> orderService.placeOrder("user@test.com", 1L, "CARD"));
	}

	@Test
	void placeOrderThrowsWhenCartEmpty() {
		cart.setCartItems(new ArrayList<>());
		when(cartRepo.findCartByEmailAndCartId("user@test.com", 1L)).thenReturn(cart);
		when(paymentRepo.save(any(Payment.class))).thenReturn(new Payment());
		when(orderRepo.save(any(Order.class))).thenReturn(new Order());

		assertThrows(APIException.class, () -> orderService.placeOrder("user@test.com", 1L, "CARD"));
	}

	@Test
	void getOrdersByUserThrowsWhenEmpty() {
		when(orderRepo.findAllByEmail("user@test.com")).thenReturn(List.of());

		assertThrows(APIException.class, () -> orderService.getOrdersByUser("user@test.com"));
	}

	@Test
	void getOrderThrowsWhenNotFound() {
		when(orderRepo.findOrderByEmailAndOrderId("user@test.com", 1L)).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder("user@test.com", 1L));
	}

	@Test
	void getAllOrdersReturnsPagedResults() {
		Order order = new Order();
		Page<Order> page = new PageImpl<>(List.of(order));
		when(orderRepo.findAll(any(Pageable.class))).thenReturn(page);
		when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

		OrderResponse response = orderService.getAllOrders(0, 10, "totalAmount", "asc");

		assertEquals(1, response.getContent().size());
	}

	@Test
	void updateOrderSucceeds() {
		Order order = new Order();
		order.setOrderId(1L);
		when(orderRepo.findOrderByEmailAndOrderId("user@test.com", 1L)).thenReturn(order);
		when(modelMapper.map(order, OrderDTO.class)).thenReturn(new OrderDTO());

		OrderDTO result = orderService.updateOrder("user@test.com", 1L, "Shipped");

		assertNotNull(result);
		assertEquals("Shipped", order.getOrderStatus());
	}

}
