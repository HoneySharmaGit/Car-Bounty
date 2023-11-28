package com.carbounty.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.carbounty.entity.Address;
import com.carbounty.entity.Roles;
import com.carbounty.entity.User;

@SuppressWarnings("serial")
public class UserDetailedModel implements Serializable {

	private int userId;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String userName;
	private boolean isEmailVerified;
	private boolean isNumberVerified;
	private List<AddressModel> addressList;
	private String profilePicture;
	private Set<String> roles;
	private String createdAt;
	private String modifiedAt;
	private boolean deleted;

	public UserDetailedModel(User user) {
		this.userId = user.getId();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.email = user.getEmail();
		this.phoneNumber = user.getPhoneNumber();
		this.userName = user.getUserName();
		this.isEmailVerified = user.isEmailVerified();
		this.isNumberVerified = user.isNumberVerified();
		this.addressList = getAddressModelList(user.getAddressList());
		this.profilePicture = user.getProfilePicture();
		this.roles = setRolesForUser(user.getRoles());
		this.createdAt = user.getCreatedAt();
		this.modifiedAt = user.getModifiedAt();
		this.deleted = user.isDeleted();
	}

	private List<AddressModel> getAddressModelList(List<Address> addressList2) {
		List<AddressModel> addressModelList = new ArrayList<>();
		for (Address address : addressList2) {
			AddressModel addressModel = new AddressModel(address);
			addressModelList.add(addressModel);
		}
		return addressModelList;
	}

	private Set<String> setRolesForUser(Set<Roles> roles2) {
		Set<String> rolesSet = new HashSet<>();
		for (Roles r : roles2) {
			rolesSet.add(r.getRole());
		}
		return rolesSet;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isEmailVerified() {
		return isEmailVerified;
	}

	public void setEmailVerified(boolean isEmailVerified) {
		this.isEmailVerified = isEmailVerified;
	}

	public boolean isNumberVerified() {
		return isNumberVerified;
	}

	public void setNumberVerified(boolean isNumberVerified) {
		this.isNumberVerified = isNumberVerified;
	}

	public List<AddressModel> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<AddressModel> addressList) {
		this.addressList = addressList;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
