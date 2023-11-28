package com.carbounty.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.carbounty.entity.StatusResponse;
import com.carbounty.jwt.JwtService;
import com.carbounty.model.EmailAndPasswordModel;
import com.carbounty.model.ResponseModel;
import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.UserModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.pagination.ObjectPagination;
import com.carbounty.service.AdminService;
import com.carbounty.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserService userService;

	@Autowired
	private AdminService adminService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/login")
	public ResponseEntity<?> LoginUserByEmailAndPassword(@RequestBody EmailAndPasswordModel emailAndPasswordModel) {
		try {
			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					emailAndPasswordModel.getEmail(), emailAndPasswordModel.getPassword()));
			String token = null;
			if (authentication.isAuthenticated()) {
				UserModel user = userService.loginUserByEmailAndPassword(emailAndPasswordModel);
				Map<String, Object> adminMap = new HashMap<>();
				adminMap.put("userId", user.getId());
				adminMap.put("firstName", user.getFirstName());
				adminMap.put("lastName", user.getLastName());
				adminMap.put("email", user.getEmail());
				adminMap.put("phoneNumber", user.getPhoneNumber());
				adminMap.put("role", user.getRoles());
				Map<String, Object> claims = new HashMap<>();
				claims.put("admin", adminMap);
				token = jwtService.generateToken(emailAndPasswordModel.getEmail(), claims);
				return ResponseEntity
						.ok(new ResponseModel(Map.of("token", token), "token provided for admin", "success"));
			} else {
				throw new UsernameNotFoundException("invalid user request !");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to upload vehicle (single)
	@PostMapping("/vehicle/upload")
	public ResponseEntity<?> singleUploadByAdmin(HttpServletRequest request,
			@ModelAttribute VehicleDetailedModel vehicleDetailedModel,
			@RequestParam(value = "vehicleImages", required = false) List<MultipartFile> vehicleImages,
			@RequestParam(value = "interiorImages", required = false) List<MultipartFile> interiorImages,
			@RequestParam(value = "exteriorImages", required = false) List<MultipartFile> exteriorImages,
			@RequestParam(value = "gallery", required = false) List<MultipartFile> gallery) {
		ResponseEntity<?> resp = null;
		try {
			adminService.singleUploadByAdmin(request, vehicleDetailedModel, vehicleImages, interiorImages,
					exteriorImages, gallery);
			resp = ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "product added by admin", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			resp = ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
		return resp;
	}

	// code to upload vehicle (bulk)
	@PostMapping("/vehicle/bulk")
	public ResponseEntity<?> bulkUploadByAdmin(
			@RequestParam(value = "excel", required = false) MultipartFile excelFile) {
		ResponseEntity<?> resp = null;
		try {
			resp = adminService.bulkUploadByAdmin(excelFile);
		} catch (Exception e) {
			e.printStackTrace();
			resp = ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
		return resp;
	}

	// code to upload images and get single String link
	@GetMapping("/upload/image")
	public ResponseEntity<?> uploadImageAndGetLink(@RequestParam("images") List<MultipartFile> images) {
		try {
			String resp = adminService.uploadImageAndGetLink(images);
			return ResponseEntity.ok(new ResponseModel(resp, "images uploaded by admin", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to fetch all the vehicles
	@GetMapping("/vehicles/page={pageCount}&size=10")
	public ResponseEntity<?> getVehiclesForAdmin(@PathVariable("pageCount") int pageCount) {
		try {
			ObjectPagination objectPagination = adminService.getVehiclesForAdmin(pageCount - 1);
			return ResponseEntity.ok(objectPagination);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to fetch vehicle by id
	@GetMapping("/vehicle/{vehicleId}")
	public ResponseEntity<?> getVehcileByIdForAdmin(@PathVariable("vehicleId") int vehicleId) {
		try {
			VehicleDetailedModel vehicleDetailedModel = adminService.getVehcileByIdForAdmin(vehicleId);
			ResponseModel resp = null;
			if (vehicleDetailedModel == null) {
				resp = new ResponseModel(vehicleDetailedModel, "vehicle by id fetched failed by admin", "error");
			} else {
				resp = new ResponseModel(vehicleDetailedModel, "vehicle by id fetched by admin", "success");
			}
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to edit vehicle
	// need to be modified, getting *ERROR*
	@PutMapping("/vehicle/{vehicleId}/edit")
	public ResponseEntity<?> editVehicleByAdmin(@PathVariable("vehicleId") int vehicleId,
			@ModelAttribute VehicleDetailedModel vehicleDetailedModelToBeUpdated,
			@RequestParam(value = "interiorImages", required = false) List<MultipartFile> interiorImages,
			@RequestParam(value = "vehicleImages", required = false) List<MultipartFile> vehicleImages,
			@RequestParam(value = "exteriorImages", required = false) List<MultipartFile> exteriorImages,
			@RequestParam(value = "gallery", required = false) List<MultipartFile> gallery) {
		try {
			String message = adminService.editVehicleByAdmin(vehicleId, vehicleDetailedModelToBeUpdated, vehicleImages,
					interiorImages, exteriorImages, gallery);
			if (message == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ResponseModel(new ArrayList<>(), "vehicle not found", "error"));
			}
			ResponseModel resp = new ResponseModel(message, "vehicle by id fetched to edit by admin", "success");
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to delete vehicle by id
	@DeleteMapping("/vehicle/{vehicle}/delete")
	public ResponseEntity<?> deleteVehicleByAdmin(@PathVariable("vehicleId") int vehicleId) {
		try {
			String message = adminService.deleteVehicleByAdmin(vehicleId);
			if (message == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new StatusResponse("error", "vehicle not found"));
			}
			ResponseModel resp = new ResponseModel(message, "vehicle by id fetched to delete by admin", "success");
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to modify the vehicle in stock
	// inStock
	@PutMapping("/vehicle/{vehicleId}/stock")
	public ResponseEntity<?> modifyVehcileInStockByAdmin(@PathVariable("vehicleId") int vehicleId,
			@RequestBody boolean inStock) {
		try {
			adminService.modifyVehcileInStockByAdmin(vehicleId, inStock);
			return ResponseEntity
					.ok(new ResponseModel(new ArrayList<>(), "vehicle inStock modified by admin", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to fetch all the users
	@GetMapping("/users/page={pageCount}&size=10")
	public ResponseEntity<?> getUsersForAdminByRole(@PathVariable("pageCount") int pageCount) {
		try {
			final String role = "USER";
			ObjectPagination objectPagination = adminService.getUsersForAdmin(pageCount - 1, role);
			return ResponseEntity.ok(objectPagination);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	// code to fetch user by id
	@GetMapping("/user/{userId}")
	public ResponseEntity<ResponseModel> getUserByIdForAdmin(@PathVariable("userId") int userId) {
		try {
			UserDetailedModel userDetailedModel = adminService.getUserByIdForAdmin(userId);
			if (userDetailedModel == null) {
				return ResponseEntity
						.ok(new ResponseModel(new ArrayList<>(), "user by id fetched failed by admin", "error"));
			} else {
				return ResponseEntity
						.ok(new ResponseModel(userDetailedModel, "user by id fetched by admin", "success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

	@PostMapping("/demoApi")
	public ResponseEntity<ResponseModel> demoApiForShivani(@RequestBody Map<String, Object> params) {
		try {
			adminService.demoApiForShivani(params);
			return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "demo api working fine", "success"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseModel(new ArrayList<>(), e.getMessage(), "error"));
		}
	}

}
