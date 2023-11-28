package com.carbounty.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.carbounty.jwt.JwtService;
import com.carbounty.model.AddressModel;
import com.carbounty.model.ChangePasswordModel;
import com.carbounty.model.EditUserModel;
import com.carbounty.model.EmailAndPasswordModel;
import com.carbounty.model.ForgetPasswordModel;
import com.carbounty.model.ResponseModel;
import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.UserModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.pagination.ObjectPagination;
import com.carbounty.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	// code to register user and send the email for email verification
	// firstName, lastName, email, phoneNumber, password
	@PostMapping("/register")
	public ResponseEntity<?> RegisterUser(final HttpServletRequest request, @RequestBody UserModel userModel) {
		ResponseEntity<?> resp = null;
		try {
			String result = userService.registerUser(userModel, request);
			if (result.equals("success")) {
				resp = ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new ResponseModel(new ArrayList<>(), "email sent for registration", "success"));
			} else {
				resp = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
						.body(new ResponseModel(new ArrayList<>(), result, "success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
		return resp;
	}

	// code to verify registration token
	// token as request param
	@PostMapping("/verifyUserRegistration")
	public ResponseEntity<?> verifyUserRegistrationToken(@RequestParam("token") String token) {
		ResponseEntity<?> resp = null;
		try {
			String result = userService.verifyUserRegistrationToken(token);
			if (result.equals("valid")) {
				resp = ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new ResponseModel(new ArrayList<>(), result, "success"));
			} else if (result.equals("invalid")) {
				resp = ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseModel(new ArrayList<>(), result, "error"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
		return resp;
	}

	// code to regenerate registration token and send to user on email
	// previous token as request param
	@PostMapping("/regenerateUserToken")
	public ResponseEntity<?> regenerateUserRegistrationToken(@RequestParam("token") String token,
			final HttpServletRequest request) {
		ResponseEntity<?> resp = null;
		try {
			String result = userService.regenerateUserRegistrationToken(token, request);
			resp = ResponseEntity.ok(new ResponseModel(new ArrayList<>(), result, "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
		return resp;
	}

	// code to login for user
	// email, password
	@PostMapping("/login")
	public ResponseEntity<?> LoginUserByEmailAndPassword(@RequestBody EmailAndPasswordModel emailAndPasswordModel) {
		try {
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					emailAndPasswordModel.getEmail(), emailAndPasswordModel.getPassword()));
			String token = null;
			if (authentication.isAuthenticated()) {
				UserModel user = userService.loginUserByEmailAndPassword(emailAndPasswordModel);
				Map<String, Object> userMap = new HashMap<>();
				userMap.put("userId", user.getId());
				userMap.put("firstName", user.getFirstName());
				userMap.put("lastName", user.getLastName());
				userMap.put("email", user.getEmail());
				userMap.put("phoneNumber", user.getPhoneNumber());
				userMap.put("role", user.getRoles());
				Map<String, Object> claims = new HashMap<>();
				claims.put("user", userMap);
				token = jwtService.generateToken(emailAndPasswordModel.getEmail(), claims);
				return ResponseEntity
						.ok(new ResponseModel(Map.of("token", token), "token provided for user", "success"));
			} else {
				throw new UsernameNotFoundException("invalid user request!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// email, password
//	@PostMapping("/login")
//	public ResponseEntity<?> LoginUserByEmailAndPassword(@RequestBody EmailAndPasswordModel emailAndPasswordModel) {
//		try {
//			UserModel user = userService.loginUserByEmailAndPassword(emailAndPasswordModel);
//			if (user != null) {
//				return ResponseEntity.ok(Map.of("user", user));
//			} else {
//				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//						.body(new StatusResponse("error", "invalid password"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(new StatusResponse("error", e.getMessage()));
//		}
//	}

	// code to get list of vehicles for user
	// pageCount (1,2,3...)
	@GetMapping("/vehicles/page={pageCount}&size=10")
	public ResponseEntity<?> getVehiclesForUser(@PathVariable("pageCount") int pageCount) {
		try {
			ObjectPagination objectPagination = userService.getVehiclesForUser(pageCount - 1);
			return ResponseEntity.ok(objectPagination);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to get vehicle details by vehicleId
	// vehicleId
	@GetMapping("/vehicle/{vehicleId}")
	public ResponseEntity<?> getVehcileForUserById(@PathVariable("vehicleId") int vehicleId) {
		try {
			VehicleDetailedModel vehicleDetailedModel = userService.getVehcileByIdForUser(vehicleId);
			ResponseModel resp = new ResponseModel(vehicleDetailedModel, "vehicle by id fetched by user", "success");
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to get the vehicle by type
	// type
	@GetMapping("/vehicle/{type}/page={pageCount}&size=5")
	public ResponseEntity<?> getVehicleByTypeForUser(@PathVariable("type") String type,
			@PathVariable("pageCount") int pageCount) {
		try {
			ObjectPagination obejctPagination = userService.getVehicleByTypeForUser(type, pageCount - 1);
			return ResponseEntity.ok(obejctPagination);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to get user profile details for user
	@GetMapping
	public ResponseEntity<?> getUserById(HttpServletRequest request) {
		try {
			UserDetailedModel userDetailedModel = userService.getUserByUserId(request);
			return ResponseEntity.ok(new ResponseModel(userDetailedModel, "user details fetched by user", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to edit user details
	// firstName, lastName, email, phoneNumber, userName
	@PutMapping("/edit")
	public ResponseEntity<?> editUserById(HttpServletRequest request, @RequestBody EditUserModel editUserModel) {
		try {
			userService.editUserById(request, editUserModel);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "details edited by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to change password
	// currentPassword, newPassword, confirmNewPassword
	@PutMapping("/password/change")
	public ResponseEntity<?> changePasswordByUser(HttpServletRequest request,
			@RequestBody ChangePasswordModel changePasswordModel) {
		try {
			userService.changePasswordByUser(request, changePasswordModel);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "password changed by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to send forget password request on email
	@PostMapping("/password/forget/request")
	public ResponseEntity<?> forgetPasswordRequestByUser(HttpServletRequest request) {
		try {
			userService.forgetPasswordRequestByUser(request);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "email sent for password forget", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code for verify forget password token
	// userForgetPasswordToken as request param
	@PostMapping("/verifyUserForgetPasswordToken")
	public ResponseEntity<?> verifyUserForgetPasswordToken(@RequestParam("token") String token) {
		try {
			String resp = userService.verifyUserForgetPasswordToken(token);
			if (resp.equals("success")) {
				return ResponseEntity
						.ok(new ResponseModel(new ArrayList<>(), "forget password token verified by user", "success"));
			} else {
				return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
						.body(new ResponseModel(new ArrayList<>(), resp, "error"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code for forget password
	// newPassword, confirmNewPassword
	@PostMapping("/password/forget")
	public ResponseEntity<?> forgetPasswordByUser(HttpServletRequest request,
			@RequestBody ForgetPasswordModel forgetPasswordModel) {
		try {
			userService.forgetPasswordByUser(request, forgetPasswordModel);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "password changed by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to add address by user
	// name, phoneNumber, line1, line2, city, state, country, pincode,
	// (latitude, longitude)
	@PostMapping("/address/add")
	public ResponseEntity<?> addAddressForUser(HttpServletRequest request, @RequestBody AddressModel addressModel) {
		try {
			userService.addAddressForUser(request, addressModel);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "address added by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to edit address by user
	// name, phoneNumber, line1, line2, city, state, country, pincode,
	// (latitude, longitude)
	@PutMapping("/address/{addressId}/edit")
	public ResponseEntity<?> editAddressForUser(@PathVariable("addressId") int addressId,
			@RequestBody AddressModel addressModel) {
		try {
			userService.editAddressForUser(addressId, addressModel);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "address edited by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to delete address by user
	// addressId
	@DeleteMapping("/address/{addressId}/delete")
	public ResponseEntity<?> deleteAddressForUser(@PathVariable("addressId") int addressId) {
		try {
			userService.deleteAddressForUser(addressId);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "address deleted by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to get the address by addressId
	// addressId
	@GetMapping("/address/{addressId}")
	public ResponseEntity<?> getAddressByIdForUser(@PathVariable("addressId") int addressId) {
		try {
			AddressModel addressModel = userService.getAddressByIdForUser(addressId);
			return ResponseEntity
					.ok(new ResponseModel(addressModel, "address by addressId fetched by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code to update profile picture
	// profilePic
	@PostMapping("/profile/upload")
	public ResponseEntity<?> uploadProfilePicForUser(HttpServletRequest request,
			@RequestParam("profilePic") MultipartFile profilePic) {
		try {
			String imageUrl = userService.uploadProfilePicForUser(request, profilePic);
			return ResponseEntity
					.ok(new ResponseModel(Map.of("profilePic", imageUrl), "address deleted by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

	// code for searching vehicle
	// searchKeyword
	@GetMapping("/search")
	public ResponseEntity<?> searchVehicleUsingKeywords(@RequestBody Map<String, Object> req) {
		try {
			String searchKeyword = (String) req.get("searchKeyword");
			Set<Map<String, Object>> data = userService.searchVehicleUsingKeywords(searchKeyword);
			return ResponseEntity.ok(new ResponseModel(data, "vehicle searched by user", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "internal server error", "error"));
		}
	}

}
