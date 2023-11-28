package com.carbounty.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.carbounty.entity.User;
import com.carbounty.repository.UserRepository;

@Component
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String email) {
		User user = userRepo.findByEmail(email);
		if (user != null) {
			return new UserInfoUserDetails(user);
		} else {
			throw new UsernameNotFoundException("user not found with email: " + email);
		}
	}

}
