package com.carbounty.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.carbounty.entity.Roles;
import com.carbounty.entity.User;

@SuppressWarnings("serial")
@Service
public class UserInfoUserDetails implements UserDetails {

	private String email;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;

	public UserInfoUserDetails() {

	}

	public UserInfoUserDetails(User userInfo) {
		email = userInfo.getEmail();
		password = userInfo.getPassword();
		authorities = getAuthorities(userInfo.getRoles());
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Set<Roles> roles) {
		return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
				.collect(Collectors.toList());
	}

}
