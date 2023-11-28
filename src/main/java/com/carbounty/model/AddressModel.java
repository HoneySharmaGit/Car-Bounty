package com.carbounty.model;

import com.carbounty.entity.Address;

public class AddressModel {

	private int id;
	private String name;
	private String phoneNumber;
	private String line1;
	private String line2;
	private String city;
	private String state;
	private String country;
	private String pincode;
	private String latitude;
	private String longitude;

	public AddressModel() {

	}

	public AddressModel(Address address) {
		this.id = address.getId();
		this.name = address.getName();
		this.phoneNumber = address.getPhoneNumber();
		this.line1 = address.getLine1();
		this.line2 = address.getLine2();
		this.city = address.getCity();
		this.state = address.getState();
		this.country = address.getCountry();
		this.pincode = address.getPincode();
		this.latitude = address.getLatitude();
		this.longitude = address.getLongitude();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "id : " + this.id + "/nname : " + this.name + "/nphoneNumber : " + this.phoneNumber + "/nline1 : "
				+ this.line1 + "/nline2 : " + this.line2 + "/ncity : " + this.city + "/nstate : " + this.state
				+ "/ncountry : " + this.country + "/npincode : " + this.pincode + "/nlatitude : " + this.latitude
				+ "/nlongitude : " + this.longitude;
	}
}
