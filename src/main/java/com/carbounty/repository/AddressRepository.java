package com.carbounty.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carbounty.entity.Address;
import com.carbounty.entity.User;

public interface AddressRepository extends JpaRepository<Address, Integer> {

	Address findById(int addressId);

	List<Address> findByAddressOfAndDeleted(User user, boolean isDeleted);
}
