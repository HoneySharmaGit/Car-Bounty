package com.carbounty.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vehicle implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vehicle_id")
	private int vehicleId;

	@Column(name = "model_name")
	private String modelName;

	@Column(name = "brand_name")
	private String brandName;

	private String description;

	@Column(name = "fuel_type")
	private String fuelType;

	private String type;

	private String transmission;

	@Column(name = "model_number")
	private String modelNumber;

	@Column(name = "engine_power")
	private String enginePower;

	private double price;

	@Column(name = "showroom_price")
	private double showroomPrice;

	@Column(name = "km_travelled")
	private int kmTravelled;

	@Column(name = "launched_year")
	private int launchedYear;

	@Column(name = "buying_year")
	private int buyingYear;

	@Column(name = "installment_price")
	private double instalmentPrice;

	private String currency;

	@Column(name = "in_stock")
	private boolean inStock = true;

	private double discount;

	@Column(name = "created_at")
	private String addedAt;

	@Column(name = "last_modified")
	private String lastModifiedAt;

	@Column(name = "uploaded_by")
	private int uploadedBy;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Vehicle_Images"))
	private List<VehicleImages> VehicleImages;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Vehicle_Interior_Images"))
	private List<InteriorImages> interiorImages;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Vehicle_Exterior_Images"))
	private List<ExteriorImages> exteriorImages;

	@Column(name = "search_string")
	private Set<String> searchString;

	@Column(name = "view_count")
	private int viewCount = 0;

	@Column(name = "vehicle_ratings")
	private double vehicleRatings = 5.0;

	@Column(name = "sales_count")
	private int salesCount;

	private int returned = 0;

	private int cancelled = 0;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Vehicle_Gallery"))
	private List<VehicleGallery> gallery;

	@Column(name = "rim_info")
	private String rimInfo;

	@Column(name = "tyres_info")
	private String tyresInfo;

//	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//	private List<Review> reviewList;

	private boolean deleted = false;

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTransmission() {
		return transmission;
	}

	public void setTransmission(String transmission) {
		this.transmission = transmission;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	public String getEnginePower() {
		return enginePower;
	}

	public void setEnginePower(String enginePower) {
		this.enginePower = enginePower;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getInstalmentPrice() {
		return instalmentPrice;
	}

	public void setInstalmentPrice(double instalmentPrice) {
		this.instalmentPrice = instalmentPrice;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public boolean isInStock() {
		return inStock;
	}

	public void setInStock(boolean inStock) {
		this.inStock = inStock;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public String getAddedAt() {
		return addedAt;
	}

	public void setAddedAt(String addedAt) {
		this.addedAt = addedAt;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}

	public int getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(int uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public List<VehicleImages> getVehicleImages() {
		return VehicleImages;
	}

	public void setVehicleImages(List<VehicleImages> vehicleImages) {
		VehicleImages = vehicleImages;
	}

	public List<InteriorImages> getInteriorImages() {
		return interiorImages;
	}

	public void setInteriorImages(List<InteriorImages> interiorImages) {
		this.interiorImages = interiorImages;
	}

	public List<ExteriorImages> getExteriorImages() {
		return exteriorImages;
	}

	public void setExteriorImages(List<ExteriorImages> exteriorImages) {
		this.exteriorImages = exteriorImages;
	}

	public Set<String> getSearchString() {
		return searchString;
	}

	public void setSearchString(Set<String> searchString) {
		this.searchString = searchString;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public double getVehicleRatings() {
		return vehicleRatings;
	}

	public void setVehicleRatings(double vehicleRatings) {
		this.vehicleRatings = vehicleRatings;
	}

	public int getSalesCount() {
		return salesCount;
	}

	public void setSalesCount(int salesCount) {
		this.salesCount = salesCount;
	}

	public int getReturned() {
		return returned;
	}

	public void setReturned(int returned) {
		this.returned = returned;
	}

	public int getCancelled() {
		return cancelled;
	}

	public void setCancelled(int cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public double getShowroomPrice() {
		return showroomPrice;
	}

	public void setShowroomPrice(double showroomPrice) {
		this.showroomPrice = showroomPrice;
	}

	public int getKmTravelled() {
		return kmTravelled;
	}

	public void setKmTravelled(int kmTravelled) {
		this.kmTravelled = kmTravelled;
	}

	public int getLaunchedYear() {
		return launchedYear;
	}

	public void setLaunchedYear(int launchedYear) {
		this.launchedYear = launchedYear;
	}

	public int getBuyingYear() {
		return buyingYear;
	}

	public void setBuyingYear(int buyingYear) {
		this.buyingYear = buyingYear;
	}

	public List<VehicleGallery> getGallery() {
		return gallery;
	}

	public void setGallery(List<VehicleGallery> gallery) {
		this.gallery = gallery;
	}

	public String getRimInfo() {
		return rimInfo;
	}

	public void setRimInfo(String rimInfo) {
		this.rimInfo = rimInfo;
	}

	public String getTyresInfo() {
		return tyresInfo;
	}

	public void setTyresInfo(String tyresInfo) {
		this.tyresInfo = tyresInfo;
	}

}
