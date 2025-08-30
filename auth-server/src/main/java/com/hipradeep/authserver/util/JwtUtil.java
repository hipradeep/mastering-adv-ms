package com.hipradeep.authserver.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private static final String SECRET = "MySuperSecretKeyForJWTGeneration12345"; // min 32 chars
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)          // add roles claim
                .setIssuer("auth-service")
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

