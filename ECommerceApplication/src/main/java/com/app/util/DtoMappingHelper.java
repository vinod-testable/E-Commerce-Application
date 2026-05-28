package com.app.util;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.app.entites.Cart;
import com.app.entites.CartItem;
import com.app.entites.User;
import com.app.payloads.AddressDTO;
import com.app.payloads.CartDTO;
import com.app.payloads.ProductDTO;
import com.app.payloads.UserDTO;

@Component
public class DtoMappingHelper {

	private final ModelMapper modelMapper;

	public DtoMappingHelper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public List<ProductDTO> toProductDTOs(List<CartItem> cartItems) {
		return cartItems.stream()
				.map(item -> modelMapper.map(item.getProduct(), ProductDTO.class))
				.collect(Collectors.toList());
	}

	public CartDTO toCartDTO(Cart cart) {
		CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
		cartDTO.setProducts(toProductDTOs(cart.getCartItems()));
		return cartDTO;
	}

	public UserDTO toUserDTOWithDetails(User user) {
		UserDTO userDTO = modelMapper.map(user, UserDTO.class);

		if (!user.getAddresses().isEmpty()) {
			userDTO.setAddress(modelMapper.map(user.getAddresses().get(0), AddressDTO.class));
		}

		userDTO.setCart(toCartDTO(user.getCart()));
		return userDTO;
	}

}
