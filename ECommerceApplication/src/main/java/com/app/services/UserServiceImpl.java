package com.app.services;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.config.AppConstants;
import com.app.entites.Cart;
import com.app.entites.CartItem;
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
import com.app.util.PaginationUtils;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private CartService cartService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private AddressHelper addressHelper;

	@Autowired
	private DtoMappingHelper dtoMappingHelper;

	@Override
	public UserDTO registerUser(UserDTO userDTO) {

		try {
			User user = modelMapper.map(userDTO, User.class);

			Cart cart = new Cart();
			user.setCart(cart);

			Role role = roleRepo.findById(AppConstants.USER_ID).get();
			user.getRoles().add(role);

			user.setAddresses(addressHelper.toAddressList(
					addressHelper.resolveOrCreateAddress(userDTO.getAddress())));

			User registeredUser = userRepo.save(user);

			cart.setUser(registeredUser);

			userDTO = modelMapper.map(registeredUser, UserDTO.class);
			userDTO.setAddress(modelMapper.map(user.getAddresses().get(0), AddressDTO.class));

			return userDTO;
		} catch (DataIntegrityViolationException e) {
			throw new APIException("User already exists with emailId: " + userDTO.getEmail());
		}

	}

	@Override
	public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		Pageable pageDetails = PaginationUtils.createPageable(pageNumber, pageSize, sortBy, sortOrder);

		Page<User> pageUsers = userRepo.findAll(pageDetails);

		List<User> users = pageUsers.getContent();

		if (users.isEmpty()) {
			throw new APIException("No User exists !!!");
		}

		List<UserDTO> userDTOs = users.stream()
				.map(dtoMappingHelper::toUserDTOWithDetails)
				.collect(Collectors.toList());

		UserResponse userResponse = new UserResponse();
		userResponse.setContent(userDTOs);
		PaginationUtils.populateUserResponse(userResponse, pageUsers);

		return userResponse;
	}

	@Override
	public UserDTO getUserById(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

		return dtoMappingHelper.toUserDTOWithDetails(user);
	}

	@Override
	public UserDTO updateUser(Long userId, UserDTO userDTO) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

		String encodedPass = passwordEncoder.encode(userDTO.getPassword());

		user.setFirstName(userDTO.getFirstName());
		user.setLastName(userDTO.getLastName());
		user.setMobileNumber(userDTO.getMobileNumber());
		user.setEmail(userDTO.getEmail());
		user.setPassword(encodedPass);

		if (userDTO.getAddress() != null) {
			user.setAddresses(addressHelper.toAddressList(
					addressHelper.resolveOrCreateAddress(userDTO.getAddress())));
		}

		return dtoMappingHelper.toUserDTOWithDetails(user);
	}

	@Override
	public String deleteUser(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

		List<CartItem> cartItems = user.getCart().getCartItems();
		Long cartId = user.getCart().getCartId();

		cartItems.forEach(item -> {
			Long productId = item.getProduct().getProductId();
			cartService.deleteProductFromCart(cartId, productId);
		});

		userRepo.delete(user);

		return "User with userId " + userId + " deleted successfully!!!";
	}

}
