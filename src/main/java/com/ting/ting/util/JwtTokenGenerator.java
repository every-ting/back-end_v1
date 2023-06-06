package com.ting.ting.util;

import com.ting.ting.exception.ErrorCode;
import com.ting.ting.exception.ServiceType;
import com.ting.ting.exception.TingApplicationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenGenerator {

    @Value("${jwt.secret}")
    private String secret;

    public String createTokenById(Long id) {
        Map<String, Object> payloads = new HashMap<>();
        payloads.put("id", id);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis());
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(payloads)
                .setExpiration(expiration)
                .setSubject("user-auto")
                .signWith(getKey())
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getKey()).build().parseClaimsJws(token).getBody();
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new TingApplicationException(ErrorCode.TOKEN_ERROR, ServiceType.UTIL, "Invalid JWT signature.");
        } catch (ExpiredJwtException e) {
            throw new TingApplicationException(ErrorCode.TOKEN_ERROR, ServiceType.UTIL, "Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            throw new TingApplicationException(ErrorCode.TOKEN_ERROR, ServiceType.UTIL, "Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            throw new TingApplicationException(ErrorCode.TOKEN_ERROR, ServiceType.UTIL, "Invalid JWT token");
        }
    }

    public Long getIdByToken(String token) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return body.get("id", Long.class);
    }

    private Key getKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
