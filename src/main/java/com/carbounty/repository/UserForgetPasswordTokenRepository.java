package com.carbounty.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carbounty.entity.UserForgetPasswordToken;

public interface UserForgetPasswordTokenRepository  extends JpaRepository<UserForgetPasswordToken, Integer>{

	UserForgetPasswordToken findByToken(String token);

}
