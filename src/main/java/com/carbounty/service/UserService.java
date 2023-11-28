package com.carbounty.service;

import java.util.Map;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.carbounty.model.AddressModel;
import com.carbounty.model.ChangePasswordModel;
import com.carbounty.model.EditUserModel;
import com.carbounty.model.EmailAndPasswordModel;
import com.carbounty.model.ForgetPasswordModel;
import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.UserModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.pagination.ObjectPagination;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

	String registerUser(UserModel userModel, HttpServletRequest request);

	ObjectPagination getVehiclesForUser(int pageCount);

	String verifyUserRegistrationToken(String token);

	String regenerateUserRegistrationToken(String token, HttpServletRequest request);

	UserModel loginUserByEmailAndPassword(EmailAndPasswordModel passwordModel);

	VehicleDetailedModel getVehcileByIdForUser(int vehicleId);

	UserDetailedModel getUserByUserId(HttpServletRequest request) throws Exception;

	void editUserById(HttpServletRequest request, EditUserModel editUserModel) throws Exception;

	void changePasswordByUser(HttpServletRequest request, ChangePasswordModel changePasswordModel) throws Exception;

	void forgetPasswordRequestByUser(HttpServletRequest request) throws Exception;

	String verifyUserForgetPasswordToken(String token) throws Exception;

	void forgetPasswordByUser(HttpServletRequest request, ForgetPasswordModel forgetPasswordModel) throws Exception;

	void addAddressForUser(HttpServletRequest request, AddressModel addressModel) throws Exception;

	void editAddressForUser(int addressId, AddressModel addressModel) throws Exception;

	void deleteAddressForUser(int addressId) throws Exception;

	String uploadProfilePicForUser(HttpServletRequest request, MultipartFile profilePic) throws Exception;

	Set<Map<String, Object>> searchVehicleUsingKeywords(String searchKeyword) throws Exception;

	AddressModel getAddressByIdForUser(int addressId) throws Exception;

	ObjectPagination getVehicleByTypeForUser(String type, int pageCount) throws Exception;

}
