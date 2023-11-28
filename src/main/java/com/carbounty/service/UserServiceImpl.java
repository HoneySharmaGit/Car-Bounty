package com.carbounty.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.carbounty.entity.Address;
import com.carbounty.entity.Roles;
import com.carbounty.entity.User;
import com.carbounty.entity.UserForgetPasswordToken;
import com.carbounty.entity.UserRegistrationToken;
import com.carbounty.entity.Vehicle;
import com.carbounty.event.ForgetPasswordEvent;
import com.carbounty.event.NewTokenRegistrationEvent;
import com.carbounty.event.PasswordChangedEvent;
import com.carbounty.event.UserRegistrationEvent;
import com.carbounty.exception.AddressNotFoundException;
import com.carbounty.exception.ForgetPasswordTokenException;
import com.carbounty.exception.PasswordNotMatchException;
import com.carbounty.exception.UserNotFoundException;
import com.carbounty.helper.AwsService;
import com.carbounty.helper.DateGenerator;
import com.carbounty.jwt.JwtService;
import com.carbounty.model.AddressModel;
import com.carbounty.model.ChangePasswordModel;
import com.carbounty.model.EditUserModel;
import com.carbounty.model.EmailAndPasswordModel;
import com.carbounty.model.ForgetPasswordModel;
import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.UserModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.model.VehicleModel;
import com.carbounty.pagination.ObjectPagination;
import com.carbounty.repository.AddressRepository;
import com.carbounty.repository.RolesRepository;
import com.carbounty.repository.UserForgetPasswordTokenRepository;
import com.carbounty.repository.UserRegistrationTokenRepository;
import com.carbounty.repository.UserRepository;
import com.carbounty.repository.VehicleRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private AddressRepository addressRepo;

	@Autowired
	private VehicleRepository vehicleRepo;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private UserRegistrationTokenRepository userRegistrationTokenRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RolesRepository rolesRepo;

	@Autowired
	private DateGenerator dateGenerator;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserForgetPasswordTokenRepository userForgetPasswordTokenRepo;

//	@Autowired
//	private GoogleDriveService drive;

	@Autowired
	private AwsService awsService;

	@Override
	public String registerUser(UserModel userModel, HttpServletRequest request) {
		User existingUser = null;
		existingUser = userRepo.findByEmail(userModel.getEmail());
		if (existingUser != null) {
			return "email already registered";
		}
		existingUser = userRepo.findByPhoneNumber(userModel.getPhoneNumber());
		if (existingUser != null) {
			return "number already registered";
		}
		User savedUser = createNewUser(userModel);
		applicationEventPublisher.publishEvent(new UserRegistrationEvent(savedUser, applicationUrl(request)));
		return "success";
	}

	private User createNewUser(UserModel userModel) {
		User user = new User();
		user.setFirstName(userModel.getFirstName());
		user.setLastName(userModel.getLastName());
		user.setEmail(userModel.getEmail());
		user.setPhoneNumber(userModel.getPhoneNumber());
		user.setPassword(passwordEncoder.encode(userModel.getPassword()));
		user.setCreatedAt(dateGenerator.dateAndTimeGenerator());
		User savedUser = userRepo.save(user);

		Roles role = new Roles();
		role.setRole("USER");
		role.setUserId(savedUser.getId());
		Roles savedRole = rolesRepo.save(role);

		Set<Roles> userRolesSet = new HashSet<>();
		userRolesSet.add(savedRole);
		savedUser.setUserName("user" + savedUser.getId());
		return userRepo.save(savedUser);

	}

	private String applicationUrl(HttpServletRequest request) {
		return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

	@Override
	public String verifyUserRegistrationToken(String token) {
		UserRegistrationToken existingUserToken = userRegistrationTokenRepo.findByToken(token);
		if (existingUserToken == null) {
			return "invalid";
		}
		Calendar calendar = Calendar.getInstance();
		if (existingUserToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
			existingUserToken.setDeleted(true);
			userRegistrationTokenRepo.save(existingUserToken);
			return "expired";
		} else {
			existingUserToken.setEnabled(true);
			UserRegistrationToken savedRegistrationToken = userRegistrationTokenRepo.save(existingUserToken);
			User user = userRepo.findById(savedRegistrationToken.getUser().getId());
			if (user != null && savedRegistrationToken.isEnabled()) {
				user.setEmailVerified(true);
				userRepo.save(user);
			}
			return "valid";
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public String regenerateUserRegistrationToken(String token, HttpServletRequest request) {
		UserRegistrationToken existingUserToken = userRegistrationTokenRepo.findByToken(token);
		String newToken = UUID.randomUUID().toString();
		existingUserToken.setToken(newToken);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(new Date().getTime());
		calendar.add(calendar.MINUTE, 10);
		Date newExpirationTime = new Date(calendar.getTime().getTime());
		existingUserToken.setExpirationTime(newExpirationTime);
		existingUserToken.setDeleted(false);
		UserRegistrationToken savedUserToken = userRegistrationTokenRepo.save(existingUserToken);
		applicationEventPublisher
				.publishEvent(new NewTokenRegistrationEvent(savedUserToken.getUser(), applicationUrl(request)));
		return "success";
	}

	@Override
	public UserModel loginUserByEmailAndPassword(EmailAndPasswordModel emailAndPasswordModel) {
		String email = emailAndPasswordModel.getEmail();
		String password = emailAndPasswordModel.getPassword();
		User user = userRepo.findByEmailOrPhoneNumber(email, email);
		if (user == null) {
			throw new UserNotFoundException("user not found");
		}
		String dbPassword = user.getPassword();
		String result = checkPassword(password, dbPassword);
		if (result.equals("valid")) {
			return new UserModel(user);
		} else {
			return null;
		}
	}

	private String checkPassword(String password, String dbPassword) {
		if (passwordEncoder.matches(password, dbPassword)) {
			return "valid";
		} else {
			return "invalid";
		}
	}

	@Override
	public ObjectPagination getVehiclesForUser(int pageCount) {
		Pageable paging = PageRequest.of(pageCount, 10, Sort.by("vehicleId").ascending());
		Page<Vehicle> vehicleList = vehicleRepo.findAllByDeleted(false, paging);
		List<VehicleModel> resp = new ArrayList<>();
		if (!vehicleList.isEmpty()) {
			for (Vehicle vehicle : vehicleList.getContent()) {
				VehicleModel vehicleModel = getVehicleModelFromVehcile(vehicle);
				resp.add(vehicleModel);
			}
		}
		ObjectPagination objectPagination = new ObjectPagination();
		objectPagination.setData(resp);
		objectPagination.setTotalPages(vehicleList.getTotalPages());
		objectPagination.setTotalElements(vehicleList.getTotalElements());
		objectPagination.setCurrentPage(vehicleList.getNumber() + 1);
		objectPagination.setPageSize(vehicleList.getSize());
		objectPagination.setMessage("vehicle data fetched by user");
		objectPagination.setStatus("success");
		return objectPagination;
	}

	private VehicleModel getVehicleModelFromVehcile(Vehicle vehicle) {
		VehicleModel model = new VehicleModel(vehicle);
		return model;
	}

	@Override
	public ObjectPagination getVehicleByTypeForUser(String type, int pageCount) throws Exception {
		Pageable paging = PageRequest.of(pageCount, 5, Sort.by("vehicleId").ascending());
		Page<Vehicle> vehicleList = vehicleRepo.findAllByDeletedAndType(false, type, paging);
		List<VehicleModel> resp = new ArrayList<>();
		if (!vehicleList.isEmpty()) {
			for (Vehicle vehicle : vehicleList.getContent()) {
				VehicleModel vehicleModel = getVehicleModelFromVehcile(vehicle);
				resp.add(vehicleModel);
			}
		}
		ObjectPagination objectPagination = new ObjectPagination();
		objectPagination.setData(resp);
		objectPagination.setTotalPages(vehicleList.getTotalPages());
		objectPagination.setTotalElements(vehicleList.getTotalElements());
		objectPagination.setCurrentPage(vehicleList.getNumber() + 1);
		objectPagination.setPageSize(vehicleList.getSize());
		objectPagination.setMessage("vehicle data by type fetched by user");
		objectPagination.setStatus("success");
		return objectPagination;
	}

	@Override
	public VehicleDetailedModel getVehcileByIdForUser(int vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId);
		VehicleDetailedModel vehicleDetailedModel = null;
		if (vehicle != null) {
			vehicleDetailedModel = new VehicleDetailedModel(vehicle);
		}
		return vehicleDetailedModel;
	}

	private Integer fetchUserIdFromRequestHeader(HttpServletRequest request) throws Exception {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new Exception("Authorization header not found");
		}
		String token = authHeader.substring(7);
		Claims claims = jwtService.extractAllClaims(token);
		@SuppressWarnings("unchecked")
		Map<String, Object> userMap = (Map<String, Object>) claims.get("user");
		System.out.println(userMap);
		return (Integer) userMap.get("userId");

	}

	@Override
	public UserDetailedModel getUserByUserId(HttpServletRequest request) throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user != null) {
			UserDetailedModel userDetailedModel = new UserDetailedModel(user);
			return userDetailedModel;
		} else {
			throw new UserNotFoundException("user not found");
		}
	}

	@Override
	public void editUserById(HttpServletRequest request, EditUserModel editUserModel) throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user != null) {
			editUserUsingUserModel(user, editUserModel);
		} else {
			throw new UserNotFoundException("user not found");
		}
	}

	private void editUserUsingUserModel(User user, EditUserModel editUserModel) throws Exception {
		if (user.getFirstName() != null && !user.getFirstName().equals(editUserModel.getFirstName())) {
			user.setFirstName(editUserModel.getFirstName());
		}
		if (user.getLastName() != null && !user.getLastName().equals(editUserModel.getLastName())) {
			user.setLastName(editUserModel.getLastName());
		}
		if (user.getEmail() != null && !user.getEmail().equals(editUserModel.getEmail())) {
			user.setEmail(editUserModel.getEmail());
			user.setEmailVerified(false);
		}
		if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(editUserModel.getPhoneNumber())) {
			user.setPhoneNumber(editUserModel.getPhoneNumber());
			user.setNumberVerified(false);
		}
		if (user.getUserName() != null && !user.getUserName().equals(editUserModel.getUserName())) {
			user.setUserName(editUserModel.getUserName());
		}
		userRepo.save(user);
	}

	@Override
	public void changePasswordByUser(HttpServletRequest request, ChangePasswordModel changePasswordModel)
			throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user != null) {
			if (!changePasswordModel.getNewPassword().equals(changePasswordModel.getConfirmNewPassword())) {
				throw new PasswordNotMatchException("password dosen't matche");
			}
			String encodedDBPassword = user.getPassword();
			String currentEncodedPassword = passwordEncoder.encode(changePasswordModel.getCurrentPassword());
			if (!encodedDBPassword.matches(currentEncodedPassword)) {
				throw new PasswordNotMatchException("invalid password");
			}
			String newEncodedPassword = passwordEncoder.encode(changePasswordModel.getNewPassword());
			user.setPassword(newEncodedPassword);
			User savedUser = userRepo.save(user);
			applicationEventPublisher.publishEvent(new PasswordChangedEvent(savedUser));
		} else {
			throw new UserNotFoundException("user not found");
		}
	}

	@Override
	public void forgetPasswordRequestByUser(HttpServletRequest request) throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user != null) {
			applicationEventPublisher.publishEvent((new ForgetPasswordEvent(user, applicationUrl(request))));
		} else {
			throw new UserNotFoundException("user not found");
		}
	}

	@Override
	public String verifyUserForgetPasswordToken(String token) throws Exception {
		UserForgetPasswordToken userForgetPasswordToken = userForgetPasswordTokenRepo.findByToken(token);
		if (userForgetPasswordToken == null) {
			throw new ForgetPasswordTokenException("forget password token not found");
		}
		Calendar calendar = Calendar.getInstance();
		if (userForgetPasswordToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
			userForgetPasswordToken.setDeleted(true);
			userForgetPasswordTokenRepo.save(userForgetPasswordToken);
			return "forget password token expired";
		} else {
			return "success";
		}
	}

	@Override
	public void forgetPasswordByUser(HttpServletRequest request, ForgetPasswordModel forgetPasswordModel)
			throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user != null) {
			if (!forgetPasswordModel.getNewPassword().equals(forgetPasswordModel.getConfirmNewPassword())) {
				throw new PasswordNotMatchException("password dosen't matche");
			}
			String encodedNewPassword = passwordEncoder.encode((forgetPasswordModel.getNewPassword()));
			user.setPassword(encodedNewPassword);
			User savedUser = userRepo.save(user);
			applicationEventPublisher.publishEvent(new PasswordChangedEvent(savedUser));
		} else {
			throw new UserNotFoundException("user not found");
		}
	}

	@Override
	public void addAddressForUser(HttpServletRequest request, AddressModel addressModel) throws Exception {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new Exception("Authorization header not found");
		}
		String token = authHeader.substring(7);
		Claims claims = jwtService.extractAllClaims(token);
		@SuppressWarnings("unchecked")
		Map<String, Object> userMap = (Map<String, Object>) claims.get("user");
		User user = userRepo.findById((int) userMap.get("userId"));
		Address savedAddress = createAddressFromAddressModel(user, addressModel);
		List<Address> list = user.getAddressList();
		list.add(savedAddress);
		user.setAddressList(list);
		userRepo.save(user);
	}

	private Address createAddressFromAddressModel(User user, AddressModel addressModel) throws Exception {
		Address address = new Address();
		if (addressModel.getName() != null) {
			address.setName(addressModel.getName());
		} else {
			address.setName(user.getFirstName() + " " + user.getLastName());
		}
		if (addressModel.getPhoneNumber() != null) {
			address.setPhoneNumber(addressModel.getPhoneNumber());
		} else {
			address.setPhoneNumber(user.getPhoneNumber());
		}
		address.setLine1(addressModel.getLine1());
		address.setLine2(addressModel.getLine2());
		address.setCity(addressModel.getCity());
		address.setState(addressModel.getState());
		address.setCountry(addressModel.getCountry());
		address.setPincode(addressModel.getPincode());
		address.setLatitude(addressModel.getLatitude());
		address.setLongitude(addressModel.getLongitude());
		address.setCreatedAt(dateGenerator.dateAndTimeGenerator());
		address.setAddressOf(user);
		return addressRepo.save(address);
	}

	@Override
	public void editAddressForUser(int addressId, AddressModel addressModel) throws Exception {
		Address address = addressRepo.findById(addressId);
		if (address == null) {
			throw new AddressNotFoundException("address not found");
		}
		editAddressFromAddressModel(addressModel, address);
	}

	private void editAddressFromAddressModel(AddressModel addressModel, Address address) {
		if (addressModel.getName() != null && !addressModel.getName().equals(address.getName())) {
			address.setName(addressModel.getName());
		}
		if (addressModel.getPhoneNumber() != null && !addressModel.getPhoneNumber().equals(address.getPhoneNumber())) {
			address.setPhoneNumber(addressModel.getPhoneNumber());
		}
		if (addressModel.getLine1() != null && !addressModel.getLine1().equals(address.getLine1())) {
			address.setLine1(addressModel.getLine1());
		}
		if (addressModel.getLine2() != null && !addressModel.getLine2().equals(address.getLine2())) {
			address.setLine2(addressModel.getLine2());
		}
		if (addressModel.getCity() != null && !addressModel.getCity().equals(address.getCity())) {
			address.setCity(addressModel.getCity());
		}
		if (addressModel.getState() != null && !addressModel.getState().equals(address.getState())) {
			address.setState(addressModel.getState());
		}
		if (addressModel.getCountry() != null && !addressModel.getCountry().equals(address.getCountry())) {
			address.setCountry(addressModel.getCountry());
		}
		if (addressModel.getPincode() != null && !addressModel.getPincode().equals(address.getPincode())) {
			address.setPincode(addressModel.getPincode());
		}
		if (addressModel.getLatitude() != null && !addressModel.getLatitude().equals(address.getLatitude())) {
			address.setLatitude(addressModel.getLatitude());
		}
		if (addressModel.getLongitude() != null && !addressModel.getLongitude().equals(address.getLongitude())) {
			address.setLongitude(addressModel.getLongitude());
		}
		address.setModifiedAt(dateGenerator.dateAndTimeGenerator());
		addressRepo.save(address);
	}

	@Override
	public void deleteAddressForUser(int addressId) throws Exception {
		Address address = addressRepo.findById(addressId);
		if (address == null) {
			throw new AddressNotFoundException("address not found");
		}
		address.setDeleted(true);
		addressRepo.save(address);

		User user = address.getAddressOf();
		List<Address> addressList = addressRepo.findByAddressOfAndDeleted(user, false);
		user.setAddressList(addressList);
		userRepo.save(user);
	}

	@Override
	public String uploadProfilePicForUser(HttpServletRequest request, MultipartFile profilePic) throws Exception {
		int userId = fetchUserIdFromRequestHeader(request);
		User user = userRepo.findById(userId);
		if (user == null) {
			throw new UserNotFoundException("user not found");
		}
		String imageUrl = awsService.uploadImage(profilePic);
		user.setProfilePicture(imageUrl);
		userRepo.save(user);
		return imageUrl;
	}

	@Override
	public Set<Map<String, Object>> searchVehicleUsingKeywords(String searchKeyword) throws Exception {
		String[] tokens = searchKeyword.split(" ");
		Set<String> tokenSet = new HashSet<>(Arrays.asList(tokens));
		Set<Vehicle> vehicleList = vehicleRepo.findBySearchStringIn(tokenSet);
		Set<Map<String, Object>> set = new HashSet<Map<String, Object>>();
		if (vehicleList.isEmpty()) {
			return set;
		}
		for (Vehicle vehicle : vehicleList) {
			Map<String, Object> map = new HashMap<>();
			String vehicleName = vehicle.getModelName();
			String vehicleImage = vehicle.getVehicleImages().get(0).getImageLink();
			map.put("vehicleName", vehicleName);
			map.put("vehicleImage", vehicleImage);
			set.add(map);
		}
		return set;
	}

	@Override
	public AddressModel getAddressByIdForUser(int addressId) throws Exception {
		Address address = addressRepo.findById(addressId);
		if (address == null) {
			throw new AddressNotFoundException("address not found");
		}
		AddressModel addressModel = new AddressModel(address);
		return addressModel;
	}

}
