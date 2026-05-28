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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.config.AppConstants;
import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Role;
import com.app.entites.User;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.AddressDTO;
import com.app.payloads.UserDTO;
import com.app.payloads.UserResponse;
import com.app.repositories.RoleRepo;
import com.app.repositories.UserRepo;
import com.app.util.AddressHelper;
import com.app.util.DtoMappingHelper;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepo userRepo;

	@Mock
	private RoleRepo roleRepo;

	@Mock
	private CartService cartService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ModelMapper modelMapper;

	@Mock
	private AddressHelper addressHelper;

	@Mock
	private DtoMappingHelper dtoMappingHelper;

	@InjectMocks
	private UserServiceImpl userService;

	private User user;
	private UserDTO userDTO;
	private AddressDTO addressDTO;

	@BeforeEach
	void setUp() {
		addressDTO = new AddressDTO(1L, "Main St", "Block A", "City", "State", "Country", "12345");

		user = new User();
		user.setUserId(1L);
		user.setEmail("user@test.com");
		user.setCart(new Cart());
		user.setAddresses(new ArrayList<>());

		userDTO = new UserDTO();
		userDTO.setEmail("user@test.com");
		userDTO.setPassword("password");
		userDTO.setAddress(addressDTO);
	}

	@Test
	void registerUserSucceeds() {
		Role role = new Role(AppConstants.USER_ID, "USER");
		Address address = new Address("Country", "State", "City", "12345", "Main St", "Block A");

		when(modelMapper.map(userDTO, User.class)).thenReturn(user);
		when(roleRepo.findById(AppConstants.USER_ID)).thenReturn(Optional.of(role));
		when(addressHelper.resolveOrCreateAddress(addressDTO)).thenReturn(address);
		when(addressHelper.toAddressList(address)).thenReturn(List.of(address));
		when(userRepo.save(any(User.class))).thenReturn(user);
		when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
		when(modelMapper.map(address, AddressDTO.class)).thenReturn(addressDTO);

		UserDTO result = userService.registerUser(userDTO);

		assertNotNull(result);
		verify(userRepo).save(any(User.class));
	}

	@Test
	void getAllUsersReturnsPagedResults() {
		Page<User> page = new PageImpl<>(List.of(user));
		when(userRepo.findAll(any(Pageable.class))).thenReturn(page);
		when(dtoMappingHelper.toUserDTOWithDetails(user)).thenReturn(userDTO);

		UserResponse response = userService.getAllUsers(0, 10, "userId", "asc");

		assertEquals(1, response.getContent().size());
	}

	@Test
	void getAllUsersThrowsWhenEmpty() {
		Page<User> page = new PageImpl<>(List.of());
		when(userRepo.findAll(any(Pageable.class))).thenReturn(page);

		assertThrows(APIException.class, () -> userService.getAllUsers(0, 10, "userId", "asc"));
	}

	@Test
	void getUserByIdSucceeds() {
		when(userRepo.findById(1L)).thenReturn(Optional.of(user));
		when(dtoMappingHelper.toUserDTOWithDetails(user)).thenReturn(userDTO);

		UserDTO result = userService.getUserById(1L);

		assertEquals("user@test.com", result.getEmail());
	}

	@Test
	void getUserByIdThrowsWhenNotFound() {
		when(userRepo.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
	}

	@Test
	void deleteUserSucceeds() {
		Cart cart = new Cart();
		cart.setCartId(2L);
		cart.setCartItems(new ArrayList<>());
		user.setCart(cart);

		when(userRepo.findById(1L)).thenReturn(Optional.of(user));

		String result = userService.deleteUser(1L);

		verify(userRepo).delete(user);
		assertEquals("User with userId 1 deleted successfully!!!", result);
	}

}
