package com.appsdeveloperblog.app.ws.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appsdeveloperblog.app.ws.io.entity.AddressEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.AddressRepository;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.service.AddressService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	AddressRepository addressRepository;

	@Override
	public List<AddressDto> getAddresses(String userId) {
		List<AddressDto> returnValue = new ArrayList<>();

		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new RuntimeException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		List<AddressEntity> addressesEntity = addressRepository.findAllByUserDetails(userEntity);

		ModelMapper modelMapper = new ModelMapper();
		for (AddressEntity address : addressesEntity) {
			returnValue.add(modelMapper.map(address, AddressDto.class));
		}

		return returnValue;
	}

	@Override
	public AddressDto getAddress(String addressId) {
		AddressDto returnValue = new AddressDto();

		AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
		ModelMapper modelMapper = new ModelMapper();
		if (addressEntity != null) {
			returnValue = modelMapper.map(addressEntity, AddressDto.class);
		}
		return returnValue;
	}

}
