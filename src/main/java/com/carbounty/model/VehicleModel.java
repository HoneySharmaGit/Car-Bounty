package com.carbounty.model;

import java.util.List;

import com.carbounty.entity.ExteriorImages;
import com.carbounty.entity.InteriorImages;
import com.carbounty.entity.Vehicle;
import com.carbounty.entity.VehicleImages;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleModel {

	private int vehicleId;

	private String modelName;

	private String brandName;

	private String description;

	private String fuelType;

	private String transmission;

	private String modelNumber;

	private String enginePower;

	private double price;

	private double instalmentPrice;

	private String currency;

	private boolean inStock;

	private double discount;

	private List<VehicleImages> vehicleImages;

	private List<InteriorImages> interiorImages;

	private List<ExteriorImages> exteriorImages;

	private double vehicleRatings;

	public VehicleModel(Vehicle vehicle) {
		this.vehicleId = vehicle.getVehicleId();
		this.modelName = vehicle.getModelName();
		this.brandName = vehicle.getBrandName();
		this.description = vehicle.getDescription();
		this.fuelType = vehicle.getFuelType();
		this.transmission = vehicle.getTransmission();
		this.modelNumber = vehicle.getModelNumber();
		this.enginePower = vehicle.getEnginePower();
		this.price = vehicle.getPrice();
		this.instalmentPrice = vehicle.getInstalmentPrice();
		this.currency = vehicle.getCurrency();
		this.inStock = vehicle.isInStock();
		this.discount = vehicle.getDiscount();
		this.vehicleRatings = vehicle.getVehicleRatings();
		this.vehicleImages = vehicle.getVehicleImages();
		this.exteriorImages = vehicle.getExteriorImages();
		this.interiorImages = vehicle.getInteriorImages();
	}

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

	public List<VehicleImages> getVehicleImages() {
		return vehicleImages;
	}

	public void setVehicleImages(List<VehicleImages> vehicleImages) {
		this.vehicleImages = vehicleImages;
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

	public double getVehicleRatings() {
		return vehicleRatings;
	}

	public void setVehicleRatings(double vehicleRatings) {
		this.vehicleRatings = vehicleRatings;
	}
}
