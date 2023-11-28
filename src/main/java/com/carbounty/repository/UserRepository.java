package com.carbounty.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.carbounty.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	User findByEmailOrPhoneNumber(String emailOrPhoneNumber, String emailOrPhoneNumber1);

	User findByEmail(String email);

	User findByPhoneNumber(String phoneNumber);

	Page<User> findAllByDeletedAndRolesRoleContains(boolean b, String role, Pageable paging);

	User findById(int userId);

}
