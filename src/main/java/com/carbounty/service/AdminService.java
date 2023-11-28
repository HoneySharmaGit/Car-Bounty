package com.carbounty.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.pagination.ObjectPagination;

import jakarta.servlet.http.HttpServletRequest;

public interface AdminService {

	void singleUploadByAdmin(HttpServletRequest request, VehicleDetailedModel vehicleModel,
			List<MultipartFile> vehicleImages, List<MultipartFile> interiorImages, List<MultipartFile> exteriorImages,
			List<MultipartFile> gallery) throws Exception;

	ResponseEntity<?> bulkUploadByAdmin(MultipartFile excelFile);

	VehicleDetailedModel getVehcileByIdForAdmin(int vehicleId);

	ObjectPagination getVehiclesForAdmin(int pageCount);

	String editVehicleByAdmin(int vehicleId, VehicleDetailedModel vehicleDetailedModelToBeUpdated,
			List<MultipartFile> vehicleImages, List<MultipartFile> interiorImages, List<MultipartFile> exteriorImages,
			List<MultipartFile> gallery) throws InterruptedException, ExecutionException;

	String deleteVehicleByAdmin(int vehicleId);

	ObjectPagination getUsersForAdmin(int pageCount, String role);

	UserDetailedModel getUserByIdForAdmin(int userId);

	String uploadImageAndGetLink(List<MultipartFile> images) throws Exception;

	void demoApiForShivani(Map<String, Object> params) throws Exception;

	void modifyVehcileInStockByAdmin(int vehicleId, boolean inStock) throws Exception;

}
