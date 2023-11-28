package com.carbounty.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.carbounty.entity.Address;
import com.carbounty.entity.Roles;
import com.carbounty.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserModel {

	private int id;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String password;
	private String userName;
	private boolean isEmailVerified;
	private boolean isNumberVerified;
	private List<Address> addressList;
	private String profilePicture;
	private Set<String> roles;

	public UserModel() {

	}

	public UserModel(User user) {
		this.id = user.getId();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.email = user.getEmail();
		this.phoneNumber = user.getPhoneNumber();
		this.userName = user.getUserName();
		this.isEmailVerified = user.isEmailVerified();
		this.isNumberVerified = user.isNumberVerified();
		this.addressList = user.getAddressList();
		this.profilePicture = user.getProfilePicture();
		this.roles = setRolesForUser(user.getRoles());
	}

	private Set<String> setRolesForUser(Set<Roles> roles2) {
		Set<String> rolesSet = new HashSet<>();
		for (Roles r : roles2) {
			rolesSet.add(r.getRole());
		}
		return rolesSet;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public List<Address> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<Address> addressList) {
		this.addressList = addressList;
	}

	public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

}
