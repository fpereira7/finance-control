package com.financecontrol.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financecontrol.dto.AuthResponse;
import com.financecontrol.dto.LoginRequest;
import com.financecontrol.dto.RegisterRequest;
import com.financecontrol.entity.User;
import com.financecontrol.exception.BusinessException;
import com.financecontrol.repository.UserRepository;
import com.financecontrol.security.JwtService;
import com.financecontrol.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationManager authenticationManager;

	@InjectMocks
	private AuthService authService;

	@Test
	void registerShouldCreateUserAndReturnToken() {
		RegisterRequest request = new RegisterRequest("user@example.com", "password123", "User");
		User saved = new User();
		saved.setId(1L);
		saved.setEmail("user@example.com");
		saved.setPasswordHash("hashed");
		saved.setName("User");

		when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenReturn(saved);
		when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");
		when(jwtService.getExpirationMs()).thenReturn(3600000L);

		AuthResponse response = authService.register(request);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());
		assertEquals("user@example.com", userCaptor.getValue().getEmail());
		assertEquals("hashed", userCaptor.getValue().getPasswordHash());
		assertEquals("jwt-token", response.accessToken());
		assertEquals("Bearer", response.tokenType());
		assertEquals(3600L, response.expiresIn());
		assertEquals(1L, response.user().id());
	}

	@Test
	void registerShouldRejectDuplicateEmail() {
		when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

		assertThrows(
				BusinessException.class,
				() -> authService.register(new RegisterRequest("user@example.com", "password123", null)));
		verify(userRepository, never()).save(any());
	}

	@Test
	void loginShouldReturnToken() {
		User user = new User();
		user.setId(1L);
		user.setEmail("user@example.com");
		user.setPasswordHash("hashed");
		user.setName("User");

		UserPrincipal principal = new UserPrincipal(1L, "user@example.com", "hashed", "User");
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
		when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(java.util.Optional.of(user));
		when(jwtService.generateToken(principal)).thenReturn("jwt-token");
		when(jwtService.getExpirationMs()).thenReturn(3600000L);

		AuthResponse response = authService.login(new LoginRequest("user@example.com", "password123"));

		assertEquals("jwt-token", response.accessToken());
		assertEquals("user@example.com", response.user().email());
	}
}
