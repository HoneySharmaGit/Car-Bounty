//package com.carbounty.jwt;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@CrossOrigin(origins = { "*" })
//@RequestMapping("/auth")
//public class JwtController {
//
//	@Autowired
//	private JwtService jwtService;
//
////	@Autowired
////	private UserRepository userRepo;
//
//	@Autowired
//	private AuthenticationManager authenticationManager;
//
//	@PostMapping("/generateToken")
//	public String authenticateAndGetToken(@RequestBody UserAuthRequest userAuthRequest) {
//		Authentication authentication = authenticationManager.authenticate(
//				new UsernamePasswordAuthenticationToken(userAuthRequest.getUsername(), userAuthRequest.getPassword()));
//		if (authentication.isAuthenticated()) {
//			Map<String, Object> claims = new HashMap<>();
//			return jwtService.generateToken(userAuthRequest.getUsername(), claims);
//		} else {
//			throw new UsernameNotFoundException("invalid user request !");
//		}
//	}
//}
