package com.appsdeveloperblog.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appsdeveloperblog.app.ws.service.AddressService;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDto;
import com.appsdeveloperblog.app.ws.shared.dto.UserDto;
import com.appsdeveloperblog.app.ws.ui.model.request.UserDetailsRequestModel;
import com.appsdeveloperblog.app.ws.ui.model.response.AddressRest;
import com.appsdeveloperblog.app.ws.ui.model.response.OperationStatusModel;
import com.appsdeveloperblog.app.ws.ui.model.response.RequestOperationStatus;
import com.appsdeveloperblog.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("users") // http://localhost:8080/users
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;
	
	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest getUser(@PathVariable String id) {
		UserRest userRest = new UserRest();
		UserDto userDto = userService.getUserByUserId(id);
		//BeanUtils.copyProperties(userDto, userRest);
		ModelMapper modelMapper = new ModelMapper();
		userRest = modelMapper.map(userDto, UserRest.class);
		
		return userRest;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		//UserDto userDto = new UserDto();
		//BeanUtils.copyProperties(userDetails, userDto);
		
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		UserDto createUser = userService.createUser(userDto);
		//BeanUtils.copyProperties(createUser, returnValue);
		returnValue = modelMapper.map(createUser, UserRest.class);

		return returnValue;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);

		UserDto updateUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updateUser, returnValue);

		return returnValue;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());
		
		userService.deleteUser(id);
		
		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		
		return returnValue;
	}
	
	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "10") int limit) {
		List<UserRest> returnValue = new ArrayList<>();
		
		if (page > 0) page -= 1;
		
		List<UserDto> users = userService.getUsers(page, limit);
		for(UserDto user: users) {
			UserRest userRest = new UserRest();
			BeanUtils.copyProperties(user, userRest);
			returnValue.add(userRest);
		}
		
		return returnValue;
	}
	
	//http://localhost:8080/users/{user_id}/addresses
	@GetMapping(path = "/{id}/addresses",
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public CollectionModel<EntityModel<AddressRest>> getUserAddresses(@PathVariable String id) {
		List<AddressRest> addressRests = new ArrayList<>();
		List<EntityModel<AddressRest>> returnValue = new ArrayList<>();
		List<AddressDto> addressesDto = addressService.getAddresses(id);
		
		if (addressesDto != null && !addressesDto.isEmpty()) {
			java.lang.reflect.Type listType = new TypeToken<List<AddressRest>>() {}.getType();
			addressRests = new ModelMapper().map(addressesDto, listType);
			
			for (AddressRest adr: addressRests) {
				//method on				
				Link selfLink = WebMvcLinkBuilder.linkTo(
						WebMvcLinkBuilder.methodOn(UserController.class)
						.getUserAddress(id, adr.getAddressId()))
						.withSelfRel();
				EntityModel<AddressRest> entityModel =  EntityModel.of(adr, selfLink);
				returnValue.add(entityModel);
			}
		}
		
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).
				slash(id).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder.linkTo(
				WebMvcLinkBuilder.methodOn(UserController.class)
						.getUserAddresses(id))
				.withSelfRel();
		
		return CollectionModel.of(returnValue, userLink, userAddressesLink);
	}
	
	//http://localhost:8080/users/{user_id}/addresses/{address_id}
		@GetMapping(path = "/{userId}/addresses/{addressId}",
				produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
		public EntityModel<AddressRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
			AddressRest returnValue = new AddressRest();
			AddressDto addressDto = addressService.getAddress(addressId);
			
			ModelMapper modelMapper = new ModelMapper();
			returnValue = modelMapper.map(addressDto, AddressRest.class);
			
			//http://localhost:8080/users/{user_id}/addresses/{address_id}
			Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).
					slash(userId).withRel("user");
//			Link userAddressesLink = WebMvcLinkBuilder.linkTo(UserController.class).
//					slash(userId).
//					slash("addresses").
//					withRel("addresses");
//			
//			Link selfLink = WebMvcLinkBuilder.linkTo(UserController.class).
//					slash(userId).
//					slash("addresses").
//					slash(addressId).
//					withSelfRel();
			
			//method on
			Link userAddressesLink = WebMvcLinkBuilder.linkTo(
					WebMvcLinkBuilder.methodOn(UserController.class)
							.getUserAddresses(userId))
					.withRel("addresses");
			
			Link selfLink = WebMvcLinkBuilder.linkTo(
					WebMvcLinkBuilder.methodOn(UserController.class)
					.getUserAddress(userId, addressId))
					.withSelfRel();
			
//			returnValue.add(userLink);
//			returnValue.add(userAddressesLink);
//			returnValue.add(selfLink);
			
			EntityModel<AddressRest> entityModel =  EntityModel.of(returnValue, Arrays.asList(userLink, userAddressesLink, selfLink));
			
			return entityModel;
		}
}
