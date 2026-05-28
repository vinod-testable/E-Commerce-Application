package com.app.util;

import java.util.List;

import org.springframework.stereotype.Component;

import com.app.entites.Address;
import com.app.payloads.AddressDTO;
import com.app.repositories.AddressRepo;

@Component
public class AddressHelper {

	private final AddressRepo addressRepo;

	public AddressHelper(AddressRepo addressRepo) {
		this.addressRepo = addressRepo;
	}

	public Address resolveOrCreateAddress(AddressDTO addressDTO) {
		String country = addressDTO.getCountry();
		String state = addressDTO.getState();
		String city = addressDTO.getCity();
		String pincode = addressDTO.getPincode();
		String street = addressDTO.getStreet();
		String buildingName = addressDTO.getBuildingName();

		Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
				country, state, city, pincode, street, buildingName);

		if (address == null) {
			address = new Address(country, state, city, pincode, street, buildingName);
			address = addressRepo.save(address);
		}

		return address;
	}

	public List<Address> toAddressList(Address address) {
		return List.of(address);
	}

}
