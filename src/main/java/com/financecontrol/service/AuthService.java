package com.financecontrol.service;

import com.financecontrol.dto.AuthResponse;
import com.financecontrol.dto.LoginRequest;
import com.financecontrol.dto.RegisterRequest;
import com.financecontrol.dto.UserResponse;
import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.UserRepository;
import com.financecontrol.security.JwtService;
import com.financecontrol.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new BusinessException("Email already registered: " + email);
		}

		User user = new User();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setName(normalizeName(request.name()));

		User saved = userRepository.save(user);
		UserPrincipal principal = toPrincipal(saved);
		return toAuthResponse(principal, saved);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.email().trim().toLowerCase(),
						request.password()));

		UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
		User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
				.orElseThrow(() -> new BusinessException("User not found"));
		return toAuthResponse(principal, user);
	}

	private AuthResponse toAuthResponse(UserPrincipal principal, User user) {
		String token = jwtService.generateToken(principal);
		return new AuthResponse(
				token,
				"Bearer",
				jwtService.getExpirationMs() / 1000,
				new UserResponse(user.getId(), user.getEmail(), user.getName()));
	}

	private UserPrincipal toPrincipal(User user) {
		return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.getName());
	}

	private String normalizeName(String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		return name.trim();
	}
}
