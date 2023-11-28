package com.carbounty.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.carbounty.config.CustomUserDetailsService;
import com.carbounty.model.ResponseModel;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// This class helps us to validate the generated jwt token
@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JwtService jwtService;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	String[] whiteListUrl = { "/user/login", "/admin/login", "/user/register", "/user/verifyUserRegistration",
			"/user/verifyUserForgetPasswordToken", "/user/search", "/admin/vehicle/upload" };

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		if (request.getRequestURI().equals("/user/login") || request.getRequestURI().equals("/admin/login")
				|| request.getRequestURI().equals("/user/register")
				|| request.getRequestURI().equals("/user/verifyUserRegistration")
				|| request.getRequestURI().equals("/user/verifyUserForgetPasswordToken")
				|| request.getRequestURI().equals("/user/search")
				|| request.getRequestURI().equals("/admin/**") ) {
			filterChain.doFilter(request, response);
		} else {
			try {
				String authHeader = request.getHeader("Authorization");
				if (authHeader == null || !authHeader.startsWith("Bearer ")) {
					throw new Exception();
				}
				String token = authHeader.substring(7);
				String username = jwtService.extractUsername(token);
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					if (jwtService.validateToken(token, userDetails)) {
						Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
						List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList());
						if ((request.getRequestURI().equals("/user/**") && roles.contains("ROLE_USER"))
								|| roles.contains("ROLE_ADMIN")) {
							UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
									userDetails, null, userDetails.getAuthorities());
							authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
							SecurityContextHolder.getContext().setAuthentication(authToken);
						} else {
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.getWriter()
									.write(new ResponseModel(new ArrayList<>(), "fetch data failed", "error")
											.convertToJson().toString());
							return;
						}
					}
				}
				filterChain.doFilter(request, response);
			} catch (ExpiredJwtException ex) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write(
						new ResponseModel(new ArrayList<>(), "Expired Token", "error").convertToJson().toString());
			} catch (MalformedJwtException ex) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write(
						new ResponseModel(new ArrayList<>(), "Invalid Token", "error").convertToJson().toString());
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write(
						new ResponseModel(new ArrayList<>(), "Invalid Request", "error").convertToJson().toString());
			}
		}
	}
}
