package com.carbounty.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carbounty.entity.UserRegistrationToken;

public interface UserRegistrationTokenRepository extends JpaRepository<UserRegistrationToken, Integer> {

	UserRegistrationToken findByToken(String token);

}
