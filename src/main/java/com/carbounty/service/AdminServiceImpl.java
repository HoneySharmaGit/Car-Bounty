package com.carbounty.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.carbounty.entity.ExteriorImages;
import com.carbounty.entity.InteriorImages;
import com.carbounty.entity.User;
import com.carbounty.entity.Vehicle;
import com.carbounty.entity.VehicleGallery;
import com.carbounty.entity.VehicleImages;
import com.carbounty.exception.NoImageException;
import com.carbounty.exception.VehicleNotFoundException;
import com.carbounty.helper.DateGenerator;
import com.carbounty.helper.ExcelHelper;
import com.carbounty.jwt.JwtService;
import com.carbounty.model.ResponseModel;
import com.carbounty.model.UserDetailedModel;
import com.carbounty.model.VehicleDetailedModel;
import com.carbounty.pagination.ObjectPagination;
import com.carbounty.repository.ExteriorImagesRepository;
import com.carbounty.repository.InteriorImagesRepository;
import com.carbounty.repository.UserRepository;
import com.carbounty.repository.VehicleGalleryRepository;
import com.carbounty.repository.VehicleImagesRepository;
import com.carbounty.repository.VehicleRepository;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private VehicleImagesRepository vehicleImagesRepo;

	@Autowired
	private VehicleRepository vehicleRepo;

	@Autowired
	private InteriorImagesRepository interiorImagesRepo;

	@Autowired
	private ExteriorImagesRepository exteriorImagesRepo;

	@Autowired
	private VehicleGalleryRepository vehicleGalleryRepo;

	@Autowired
	private GoogleDriveService drive;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private DateGenerator dateGenerator;

	@Autowired
	private ExcelHelper excelHelper;

	@Autowired
	private JwtService jwtService;

	@Override
	public void singleUploadByAdmin(HttpServletRequest request, VehicleDetailedModel vehicleDetailedModel,
			List<MultipartFile> vehicleImages, List<MultipartFile> interiorImages, List<MultipartFile> exteriorImages,
			List<MultipartFile> gallery) throws Exception {
		try {
			// new product
			Vehicle vehicle = makeVehicleObject(vehicleDetailedModel);
			int adminId = fetchAdminIdFromRequestHeader(request);
			vehicle.setUploadedBy(adminId);
			Vehicle savedVehicle = vehicleRepo.save(vehicle);

			if (vehicleImages != null && !vehicleImages.isEmpty()) {
				List<VehicleImages> list = new ArrayList<>();
				for (MultipartFile image : vehicleImages) {
					String imageUrl = uploadImage(image);
					VehicleImages vehicleImageObject = new VehicleImages();
					vehicleImageObject.setImageLink(imageUrl);
					vehicleImageObject.setVehicleId(savedVehicle.getVehicleId());
					list.add(vehicleImagesRepo.save(vehicleImageObject));

				}
				savedVehicle.setVehicleImages(list);
			}
			if (interiorImages != null && !interiorImages.isEmpty()) {
				List<InteriorImages> list = new ArrayList<>();
				for (MultipartFile image : interiorImages) {
					String imageUrl = uploadImage(image);
					InteriorImages interiorImageObject = new InteriorImages();
					interiorImageObject.setImageLink(imageUrl);
					interiorImageObject.setVehicleId(savedVehicle.getVehicleId());
					list.add(interiorImagesRepo.save(interiorImageObject));
				}
				savedVehicle.setInteriorImages(list);
			}
			if (exteriorImages != null && !exteriorImages.isEmpty()) {
				List<ExteriorImages> list = new ArrayList<>();
				for (MultipartFile image : exteriorImages) {
					String imageUrl = uploadImage(image);
					ExteriorImages exteriorImageObject = new ExteriorImages();
					exteriorImageObject.setImageLink(imageUrl);
					exteriorImageObject.setVehicleId(savedVehicle.getVehicleId());
					list.add(exteriorImagesRepo.save(exteriorImageObject));
				}
				savedVehicle.setExteriorImages(list);
			}
			if (gallery != null && !gallery.isEmpty()) {
				List<VehicleGallery> list = new ArrayList<>();
				for (MultipartFile image : exteriorImages) {
					String imageUrl = uploadImage(image);
					VehicleGallery vehicleGalleryObject = new VehicleGallery();
					vehicleGalleryObject.setImageLink(imageUrl);
					vehicleGalleryObject.setVehicleId(savedVehicle.getVehicleId());
					list.add(vehicleGalleryRepo.save(vehicleGalleryObject));
				}
				savedVehicle.setGallery(list);
			}
			vehicleRepo.save(savedVehicle);
		} catch (Exception e) {
			throw e;
		}
	}

	private Integer fetchAdminIdFromRequestHeader(HttpServletRequest request) throws Exception {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new Exception("Authorization header not found");
		}
		String token = authHeader.substring(7);
		Claims claims = jwtService.extractAllClaims(token);
		@SuppressWarnings("unchecked")
		Map<String, Object> userMap = (Map<String, Object>) claims.get("user");
		System.out.println(userMap);
		return (Integer) userMap.get("adminId");

	}

	@Override
	public ResponseEntity<?> bulkUploadByAdmin(MultipartFile excelFile) {
		if (excelFile == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "file not found", "error"));
		}
		// validate excel is valid or not
		boolean isValid = excelHelper.checkExcelFormate(excelFile);
		if (!isValid) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseModel(new ArrayList<>(), "invalid excel format", "error"));
		}
		List<Integer> errorVehicleIndices = uploadVehicleFromExcel(excelFile);
		if (!errorVehicleIndices.isEmpty()) {
			StringBuilder errors = new StringBuilder("Error uploading vehicles at index ");
			for (Integer index : errorVehicleIndices) {
				errors.append(index).append(", ");
			}
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ResponseModel(new ArrayList<>(), errors.toString(), "error"));
		}
		return ResponseEntity.ok(new ResponseModel(new ArrayList<>(), "vehicles uploaded by admin", "success"));
	}

	private List<Integer> uploadVehicleFromExcel(MultipartFile excelFile) {
		List<Integer> errorIndices = new ArrayList<>();
		try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
			Sheet sheet = workbook.getSheet("Sheet1");
			ExecutorService executor = Executors.newFixedThreadPool(10);
			List<Future<Void>> futureList = new ArrayList<>();
			for (Row currentRow : sheet) {
				// skip header of excel
				if (currentRow.getRowNum() == 0) {
					continue;
				}
				Callable<Void> task = () -> {
					try {
						addVehicleInDBFromRow(currentRow);
					} catch (Exception e) {
						errorIndices.add(currentRow.getRowNum() + 1);
					}
					return null;
				};
				Future<Void> future = executor.submit(task);
				futureList.add(future);
			}
			for (Future<Void> future : futureList) {
				future.get();
			}
			executor.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return errorIndices;
	}

	private void addVehicleInDBFromRow(Row currentRow) throws Exception {
		Vehicle vehicle = new Vehicle();
		for (Cell currentCell : currentRow) {
			// vehicle details to be written here.
			switch (currentCell.getColumnIndex()) {
			case 0: {
				vehicle.setBrandName(currentCell.getStringCellValue());
				break;
			}
			case 1: {
				vehicle.setDescription(currentCell.getStringCellValue());
				break;
			}
			case 2: {
				vehicle.setDiscount(currentCell.getNumericCellValue());
				break;
			}
			case 3: {
				vehicle.setEnginePower(currentCell.getStringCellValue());
				break;
			}
			case 4: {
				vehicle.setFuelType(currentCell.getStringCellValue());
				break;
			}
			case 5: {
				vehicle.setPrice(currentCell.getNumericCellValue());
				break;
			}
			case 6: {
				vehicle.setInstalmentPrice(currentCell.getNumericCellValue());
				break;
			}
			case 7: {
				vehicle.setShowroomPrice(currentCell.getNumericCellValue());
				break;
			}
			case 8: {
				vehicle.setModelName(currentCell.getStringCellValue());
				break;
			}
			case 9: {
				vehicle.setModelNumber(currentCell.getStringCellValue());
				break;
			}
			case 10: {
				vehicle.setTransmission(currentCell.getStringCellValue());
				break;
			}
			case 11: {
				vehicle.setBuyingYear((int) currentCell.getNumericCellValue());
				break;
			}
			case 12: {
				vehicle.setLaunchedYear((int) currentCell.getNumericCellValue());
				break;
			}
			case 13: {
				vehicle.setKmTravelled((int) currentCell.getNumericCellValue());
				break;
			}
			case 14: {
				vehicle.setRimInfo(currentCell.getStringCellValue());
				break;
			}
			case 15: {
				vehicle.setTyresInfo(currentCell.getStringCellValue());
				break;
			}
			case 16: {
				String[] vehicleImages = (currentCell.getStringCellValue()).split(", ");
				List<VehicleImages> imageList = new ArrayList<>();
				for (String image : vehicleImages) {
					VehicleImages vehicleImage = new VehicleImages();
					vehicleImage.setImageLink(image);
					imageList.add(vehicleImagesRepo.save(vehicleImage));
				}
				vehicle.setVehicleImages(imageList);
				break;
			}
			case 17: {
				String[] exteriorImages = (currentCell.getStringCellValue()).split(", ");
				List<ExteriorImages> imageList = new ArrayList<>();
				for (String image : exteriorImages) {
					ExteriorImages exteriorImage = new ExteriorImages();
					exteriorImage.setImageLink(image);
					imageList.add(exteriorImagesRepo.save(exteriorImage));
				}
				vehicle.setExteriorImages(imageList);
				break;
			}
			case 18: {
				String[] interiorImages = (currentCell.getStringCellValue()).split(", ");
				List<InteriorImages> imageList = new ArrayList<>();
				for (String image : interiorImages) {
					InteriorImages interiorImage = new InteriorImages();
					interiorImage.setImageLink(image);
					imageList.add(interiorImagesRepo.save(interiorImage));
				}
				vehicle.setInteriorImages(imageList);
				break;
			}
			case 19: {
				String[] galleryImages = (currentCell.getStringCellValue()).split(", ");
				List<VehicleGallery> imageList = new ArrayList<>();
				for (String image : galleryImages) {
					VehicleGallery galleryImage = new VehicleGallery();
					galleryImage.setImageLink(image);
					imageList.add(vehicleGalleryRepo.save(galleryImage));
				}
				vehicle.setGallery(imageList);
				break;
			}
			default:
				break;
			}
		}
		vehicleRepo.save(vehicle);
	}

	private Vehicle makeVehicleObject(VehicleDetailedModel vehicleDetailedModel) {
		Vehicle vehicle = new Vehicle();
		vehicle.setModelName(vehicleDetailedModel.getModelName());
		vehicle.setBrandName(vehicleDetailedModel.getBrandName());
		vehicle.setDescription(vehicleDetailedModel.getDescription());
		vehicle.setFuelType(vehicleDetailedModel.getFuelType());
		vehicle.setType(vehicleDetailedModel.getType());
		vehicle.setTransmission(vehicleDetailedModel.getTransmission());
		vehicle.setModelNumber(vehicleDetailedModel.getModelNumber());
		vehicle.setEnginePower(vehicleDetailedModel.getEnginePower());
		vehicle.setPrice(vehicleDetailedModel.getPrice());
		vehicle.setShowroomPrice(vehicleDetailedModel.getShowroomPrice());
		vehicle.setKmTravelled(vehicleDetailedModel.getKmTravelled());
		vehicle.setLaunchedYear(vehicleDetailedModel.getLaunchedYear());
		vehicle.setBuyingYear(vehicleDetailedModel.getLaunchedYear());
		vehicle.setInstalmentPrice(vehicleDetailedModel.getInstalmentPrice());
		vehicle.setCurrency("INR");
		vehicle.setInStock(vehicleDetailedModel.isInStock());
		vehicle.setDiscount(vehicleDetailedModel.getDiscount());
		vehicle.setTyresInfo(vehicleDetailedModel.getTyresInfo());
		vehicle.setRimInfo(vehicleDetailedModel.getRimInfo());
		vehicle.setAddedAt(dateGenerator.dateAndTimeGenerator());
		vehicle.setSearchString(createSearchString(vehicleDetailedModel.getModelName() + " "
				+ vehicleDetailedModel.getBrandName() + " " + vehicleDetailedModel.getFuelType() + " "
				+ vehicleDetailedModel.getType() + " " + vehicleDetailedModel.getTransmission()));
		return vehicle;
	}

	private Set<String> createSearchString(String searchString) {
		String[] tokenSet = searchString.split(" ");
		Set<String> searchSet = new HashSet<>(Arrays.asList(tokenSet));
		return searchSet;
	}

	private String uploadImage(MultipartFile image) {
		String imageName = image.getOriginalFilename() + System.currentTimeMillis();
		File file = new File(imageName);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(image.getBytes());
			fos.flush();
			fos.close();

			// uploading image on AWS
			// return uploadImageInAmazonS3(bucketName, imageName, file);

			// uploading image on google drive
			return uploadImageInGoogleDrive(image);
		} catch (Exception e) {
			return e.getMessage();
		}
	}

//	private String uploadImageInAmazonS3(String bucketName, String imageName, File file) throws Exception {
//		s3Client.putObject(new PutObjectRequest(bucketName, imageName, file));
//		return s3Client.getUrl(bucketName, imageName).toString();
//	}

	private String uploadImageInGoogleDrive(MultipartFile image) {
		try {
			String result = drive.uploadGoogleDriveFile(image);
			System.out.println(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}

	@Override
	public ObjectPagination getVehiclesForAdmin(int pageCount) {
		Pageable paging = PageRequest.of(pageCount, 10, Sort.by("vehicleId").descending());
		Page<Vehicle> vehicleList = vehicleRepo.findAllByDeleted(false, paging);
		System.out.println("honeyyyyyyy");
		List<Map<String, Object>> resp = new ArrayList<>();
		if (!vehicleList.isEmpty()) {
			for (Vehicle vehicle : vehicleList.getContent()) {
				Map<String, Object> vehicleMap = getVehicleMapFromVehcile(vehicle);
				resp.add(vehicleMap);
			}
		}
		ObjectPagination objectPagination = new ObjectPagination();
		objectPagination.setData(resp);
		objectPagination.setTotalPages(vehicleList.getTotalPages());
		objectPagination.setTotalElements(vehicleList.getTotalElements());
		objectPagination.setCurrentPage(vehicleList.getNumber() + 1);
		objectPagination.setPageSize(vehicleList.getSize());
		objectPagination.setMessage("vehicle data fetched by admin");
		objectPagination.setStatus("success");
		return objectPagination;
	}

	private Map<String, Object> getVehicleMapFromVehcile(Vehicle vehicle) {
		Map<String, Object> map = new HashMap<>();
		map.put("vehicleId", vehicle.getVehicleId());
		map.put("modelName", vehicle.getModelName());
		map.put("brandName", vehicle.getBrandName());
		map.put("price", vehicle.getPrice());
		map.put("showroomPrice", vehicle.getShowroomPrice());
		map.put("uploadedBy", vehicle.getUploadedBy());
		map.put("vehicleImages", vehicle.getVehicleImages());
		map.put("inStock", vehicle.isInStock());
		return map;
	}

	@Override
	public VehicleDetailedModel getVehcileByIdForAdmin(int vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId);
		VehicleDetailedModel vehicleDetailedModel = null;
		if (vehicle != null) {
			vehicleDetailedModel = new VehicleDetailedModel(vehicle);
		}
		return vehicleDetailedModel;
	}

	@Override
	public String editVehicleByAdmin(int vehicleId, VehicleDetailedModel vehicleDetailedModelToBeUpdated,
			List<MultipartFile> vehicleImages, List<MultipartFile> interiorImages, List<MultipartFile> exteriorImages,
			List<MultipartFile> gallery) throws InterruptedException, ExecutionException {
		Vehicle vehicle = vehicleRepo.findById(vehicleId);
		if (vehicle != null) {
			ExecutorService executor = Executors.newFixedThreadPool(4);
			List<Future<String>> futureImageList = new ArrayList<>();
			if (!vehicleImages.isEmpty() && vehicleImages != null) {
				List<VehicleImages> list = vehicle.getVehicleImages();
				for (MultipartFile image : vehicleImages) {
					Future<String> imageUrl = executor.submit(() -> uploadImage(image));
					futureImageList.add(imageUrl);
				}
				for (Future<String> imageFuture : futureImageList) {
					VehicleImages obj = makeVehicleImageObject(imageFuture.get(), vehicleId);
					list.add(obj);
				}
				vehicle.setVehicleImages(list);
				futureImageList.clear();
			}
			if (!interiorImages.isEmpty() && interiorImages != null) {
				List<InteriorImages> list = vehicle.getInteriorImages();
				for (MultipartFile image : interiorImages) {
					Future<String> imageUrl = executor.submit(() -> uploadImage(image));
					futureImageList.add(imageUrl);
				}
				for (Future<String> imageFuture : futureImageList) {
					InteriorImages obj = makeInteriorImageObject(imageFuture.get(), vehicleId);
					list.add(obj);
				}
				vehicle.setInteriorImages(list);
				futureImageList.clear();
			}
			if (!exteriorImages.isEmpty() && exteriorImages != null) {
				List<ExteriorImages> list = vehicle.getExteriorImages();
				for (MultipartFile image : exteriorImages) {
					Future<String> imageUrl = executor.submit(() -> uploadImage(image));
					futureImageList.add(imageUrl);
				}
				for (Future<String> imageFuture : futureImageList) {
					ExteriorImages obj = makeExteriorImageObject(imageFuture.get(), vehicleId);
					list.add(obj);
				}
				vehicle.setExteriorImages(list);
				futureImageList.clear();
			}
			if (!gallery.isEmpty() && gallery != null) {
				List<VehicleGallery> list = vehicle.getGallery();
				for (MultipartFile image : exteriorImages) {
					Future<String> imageUrl = executor.submit(() -> uploadImage(image));
					futureImageList.add(imageUrl);
				}
				for (Future<String> imageFuture : futureImageList) {
					VehicleGallery obj = makeVehicleGalleryObject(imageFuture.get(), vehicleId);
					list.add(obj);
				}
				vehicle.setGallery(list);
				futureImageList.clear();
			}
			if (vehicleDetailedModelToBeUpdated != null) {
				vehicle = updateVehicleDetailsAdmin(vehicleDetailedModelToBeUpdated, vehicle);
			}
			vehicle.setLastModifiedAt(dateGenerator.dateAndTimeGenerator());
			vehicleRepo.save(vehicle);
			executor.shutdown();
			return "vehicle updated";
		} else {
			return null;
		}
	}

	private VehicleImages makeVehicleImageObject(String imageUrl, int vehicleId) {
		VehicleImages vehicleImageObject = new VehicleImages();
		vehicleImageObject.setImageLink(imageUrl);
		vehicleImageObject.setVehicleId(vehicleId);
		return vehicleImageObject;
	}

	private InteriorImages makeInteriorImageObject(String imageUrl, int vehicleId) {
		InteriorImages interiorImageObject = new InteriorImages();
		interiorImageObject.setImageLink(imageUrl);
		interiorImageObject.setVehicleId(vehicleId);
		return interiorImageObject;
	}

	private ExteriorImages makeExteriorImageObject(String imageUrl, int vehicleId) {
		ExteriorImages exteriorImageObject = new ExteriorImages();
		exteriorImageObject.setImageLink(imageUrl);
		exteriorImageObject.setVehicleId(vehicleId);
		return exteriorImageObject;
	}

	private VehicleGallery makeVehicleGalleryObject(String imageUrl, int vehicleId) {
		VehicleGallery vehicleGalleryObject = new VehicleGallery();
		vehicleGalleryObject.setImageLink(imageUrl);
		vehicleGalleryObject.setVehicleId(vehicleId);
		return vehicleGalleryObject;
	}

	private Vehicle updateVehicleDetailsAdmin(VehicleDetailedModel vehicleDetailedModelToBeUpdated, Vehicle vehicle) {
		if (vehicleDetailedModelToBeUpdated.getModelName() != null
				&& !vehicleDetailedModelToBeUpdated.getModelName().equals(vehicle.getModelName())) {
			vehicle.setModelName(vehicleDetailedModelToBeUpdated.getModelName());
		}
		if (vehicleDetailedModelToBeUpdated.getBrandName() != null
				&& !vehicleDetailedModelToBeUpdated.getBrandName().equals(vehicle.getBrandName())) {
			vehicle.setBrandName(vehicleDetailedModelToBeUpdated.getBrandName());
		}
		if (vehicleDetailedModelToBeUpdated.getDescription() != null
				&& !vehicleDetailedModelToBeUpdated.getDescription().equals(vehicle.getDescription())) {
			vehicle.setDescription(vehicleDetailedModelToBeUpdated.getDescription());
		}
		if (vehicleDetailedModelToBeUpdated.getFuelType() != null
				&& !vehicleDetailedModelToBeUpdated.getFuelType().equals(vehicle.getFuelType())) {
			vehicle.setFuelType(vehicleDetailedModelToBeUpdated.getFuelType());
		}
		if (vehicleDetailedModelToBeUpdated.getTransmission() != null
				&& !vehicleDetailedModelToBeUpdated.getTransmission().equals(vehicle.getTransmission())) {
			vehicle.setTransmission(vehicleDetailedModelToBeUpdated.getTransmission());
		}
		if (vehicleDetailedModelToBeUpdated.getModelNumber() != null
				&& !vehicleDetailedModelToBeUpdated.getModelNumber().equals(vehicle.getModelNumber())) {
			vehicle.setModelNumber(vehicleDetailedModelToBeUpdated.getModelNumber());
		}
		if (vehicleDetailedModelToBeUpdated.getEnginePower() != null
				&& !vehicleDetailedModelToBeUpdated.getEnginePower().equals(vehicle.getEnginePower())) {
			vehicle.setEnginePower(vehicleDetailedModelToBeUpdated.getEnginePower());
		}
		if (vehicleDetailedModelToBeUpdated.getPrice() != 0.0
				&& vehicleDetailedModelToBeUpdated.getPrice() != vehicle.getPrice()) {
			vehicle.setPrice(vehicleDetailedModelToBeUpdated.getPrice());
		}
		if (vehicleDetailedModelToBeUpdated.getShowroomPrice() != 0.0
				&& vehicleDetailedModelToBeUpdated.getShowroomPrice() != vehicle.getShowroomPrice()) {
			vehicle.setShowroomPrice(vehicleDetailedModelToBeUpdated.getShowroomPrice());
		}
		if (vehicleDetailedModelToBeUpdated.getKmTravelled() != 0
				&& vehicleDetailedModelToBeUpdated.getKmTravelled() != vehicle.getKmTravelled()) {
			vehicle.setKmTravelled(vehicleDetailedModelToBeUpdated.getKmTravelled());
		}
		if (vehicleDetailedModelToBeUpdated.getLaunchedYear() != 0
				&& vehicleDetailedModelToBeUpdated.getLaunchedYear() != vehicle.getLaunchedYear()) {
			vehicle.setLaunchedYear(vehicleDetailedModelToBeUpdated.getLaunchedYear());
		}
		if (vehicleDetailedModelToBeUpdated.getBuyingYear() != 0
				&& vehicleDetailedModelToBeUpdated.getBuyingYear() != vehicle.getBuyingYear()) {
			vehicle.setBuyingYear(vehicleDetailedModelToBeUpdated.getBuyingYear());
		}
		if (vehicleDetailedModelToBeUpdated.getInstalmentPrice() != 0.0
				&& vehicleDetailedModelToBeUpdated.getInstalmentPrice() != vehicle.getInstalmentPrice()) {
			vehicle.setInstalmentPrice(vehicleDetailedModelToBeUpdated.getInstalmentPrice());
		}
		if (vehicleDetailedModelToBeUpdated.getCurrency() != null
				&& !vehicleDetailedModelToBeUpdated.getCurrency().equals(vehicle.getCurrency())) {
			vehicle.setCurrency(vehicleDetailedModelToBeUpdated.getCurrency());
		}
		if (vehicleDetailedModelToBeUpdated.isInStock() != vehicle.isInStock()) {
			vehicle.setInStock(vehicleDetailedModelToBeUpdated.isInStock());
		}
		if (vehicleDetailedModelToBeUpdated.getDiscount() != 0.0
				&& vehicleDetailedModelToBeUpdated.getDiscount() != vehicle.getDiscount()) {
			vehicle.setDiscount(vehicleDetailedModelToBeUpdated.getDiscount());
		}
		if (vehicleDetailedModelToBeUpdated.getRimInfo() != null
				&& !vehicleDetailedModelToBeUpdated.getRimInfo().equals(vehicle.getRimInfo())) {
			vehicle.setRimInfo(vehicleDetailedModelToBeUpdated.getRimInfo());
		}
		if (vehicleDetailedModelToBeUpdated.getTyresInfo() != null
				&& !vehicleDetailedModelToBeUpdated.getTyresInfo().equals(vehicle.getTyresInfo())) {
			vehicle.setTyresInfo(vehicleDetailedModelToBeUpdated.getTyresInfo());
		}
		vehicle.setSearchString(createSearchString(vehicleDetailedModelToBeUpdated.getModelName() + " "
				+ vehicleDetailedModelToBeUpdated.getBrandName() + " " + vehicleDetailedModelToBeUpdated.getFuelType()
				+ " " + vehicleDetailedModelToBeUpdated.getType() + " "
				+ vehicleDetailedModelToBeUpdated.getTransmission()));
		return vehicle;
	}

	@Override
	public String deleteVehicleByAdmin(int vehicleId) {
		Vehicle vehicle = vehicleRepo.findById(vehicleId);
		if (vehicle != null) {
			vehicle.setDeleted(true);
			vehicleRepo.save(vehicle);
			return "vehicle deleted";
		} else {
			return null;
		}
	}

	@Override
	public ObjectPagination getUsersForAdmin(int pageCount, String role) {
		Pageable paging = PageRequest.of(pageCount, 10, Sort.by("id").ascending());
		Page<User> userList = userRepo.findAllByDeletedAndRolesRoleContains(false, role, paging);
		List<Map<String, Object>> resp = new ArrayList<>();
		if (!userList.isEmpty()) {
			for (User user : userList.getContent()) {
				Map<String, Object> userMap = getUserMapFromUser(user);
				resp.add(userMap);
			}
		}
		ObjectPagination objectPagination = new ObjectPagination();
		objectPagination.setData(resp);
		objectPagination.setTotalPages(userList.getTotalPages());
		objectPagination.setTotalElements(userList.getTotalElements());
		objectPagination.setCurrentPage(userList.getNumber() + 1);
		objectPagination.setPageSize(userList.getSize());
		objectPagination.setMessage("user data fetched by admin");
		objectPagination.setStatus("success");
		return objectPagination;
	}

	private Map<String, Object> getUserMapFromUser(User user) {
		Map<String, Object> map = new HashMap<>();
		map.put("vehicleId", user.getId());
		map.put("firstName", user.getFirstName());
		map.put("lastName", user.getLastName());
		map.put("email", user.getEmail());
		map.put("phoneNumber", user.getPhoneNumber());
		map.put("profilePicture", user.getProfilePicture());
		return map;
	}

	@Override
	public UserDetailedModel getUserByIdForAdmin(int userId) {
		User user = userRepo.findById(userId);
		UserDetailedModel userDetailedModel = null;
		if (user != null) {
			userDetailedModel = new UserDetailedModel(user);
		}
		return userDetailedModel;
	}

	@Override
	public String uploadImageAndGetLink(List<MultipartFile> images) throws Exception {
		if (!images.isEmpty()) {
			ExecutorService executor = Executors.newFixedThreadPool(10);
			List<Future<String>> futureList = new ArrayList<>();
			for (MultipartFile image : images) {
				Future<String> futureImage = executor.submit(() -> uploadImage(image));
				futureList.add(futureImage);
			}
			executor.shutdown();
			StringBuilder sb = new StringBuilder();
			for (Future<String> future : futureList) {
				sb.append(future.get() + ", ");
			}
			return sb.toString();
		}
		throw new NoImageException("please provide images");
	}

	@Override
	public void demoApiForShivani(Map<String, Object> params) throws Exception {
		@SuppressWarnings("unchecked")
		List<HashMap<String, Object>> itemsList = (List<HashMap<String, Object>>) params.get("items");
		for (HashMap<String, Object> item : itemsList) {
			int accountId = (int) item.get("accountId");
			String name = (String) item.get("name");
			String description = (String) item.get("description");
			System.out.println(accountId + " .... " + name + " .... " + description);
		}
	}

	@Override
	public void modifyVehcileInStockByAdmin(int vehicleId, boolean inStock) throws Exception {
		Vehicle vehicle = vehicleRepo.findById(vehicleId);
		if (vehicle == null) {
			throw new VehicleNotFoundException("vehicle not found");
		}
		vehicle.setInStock(inStock);
		vehicleRepo.save(vehicle);
	}

}
