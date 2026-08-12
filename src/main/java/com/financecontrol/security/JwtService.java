package com.financecontrol.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.secretKey = buildKey(secret);
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserPrincipal principal) {
		Date now = new Date();
		Date expiresAt = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.subject(principal.getUsername())
				.claim("uid", principal.getId())
				.issuedAt(now)
				.expiration(expiresAt)
				.signWith(secretKey)
				.compact();
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserPrincipal principal) {
		String email = extractEmail(token);
		return email.equalsIgnoreCase(principal.getUsername()) && !isExpired(token);
	}

	public long getExpirationMs() {
		return expirationMs;
	}

	private boolean isExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey buildKey(String secret) {
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(secret);
		}
		catch (Exception ex) {
			keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		}
		if (keyBytes.length < 32) {
			byte[] padded = new byte[32];
			System.arraycopy(keyBytes, 0, padded, 0, Math.min(keyBytes.length, 32));
			keyBytes = padded;
		}
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
