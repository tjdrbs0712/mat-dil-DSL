package com.sparta.mat_dil.jwt;

import com.sparta.mat_dil.entity.UserType;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 리프레시 헤더 값
    public static final String REFRESH_HEADER = "RefreshToken";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";
    // 토큰 만료시간
    private final long TOKEN_TIME = 60 * 60 * 1000L;
    // 리프레시 토큰 만료시간 (7일)
    private static final long REFRESH_TOKEN_TIME = 7 * 24 * 60 * 60 * 1000L;
    // 로그아웃 토큰 블랙리스트
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성 공통 로직
    private String createToken(String subject, long expirationTime, UserType userType) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .claim(AUTHORIZATION_KEY, userType)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    // 액세스 토큰 생성
    public String createAccessToken(String accountId, UserType userType) {
        return BEARER_PREFIX + createToken(accountId, TOKEN_TIME, userType);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(String accountId, UserType userType) {
        return BEARER_PREFIX + createToken(accountId, REFRESH_TOKEN_TIME, userType);
    }

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성하는 메서드
    public String generateAccessToken(String refreshToken) {
        Claims claims = getUserInfoFromToken(refreshToken);
        String username = claims.getSubject();
        UserType userType = UserType.valueOf(claims.get(AUTHORIZATION_KEY).toString());
        return createAccessToken(username, userType);
    }

    // JWT Cookie에 저장
    public void addJwtToCookie(String accessToken, String refreshToken, HttpServletResponse res) {
        try {
            setCookie(res, AUTHORIZATION_HEADER, accessToken);
            setCookie(res, REFRESH_HEADER, refreshToken);
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding JWT to cookie", e);
        }
    }

    // 쿠키 설정
    private void setCookie(HttpServletResponse res, String name, String value) throws UnsupportedEncodingException {
        value = URLEncoder.encode(value, "utf-8").replaceAll("\\+", "%20");
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(BEARER_PREFIX.length());
        }
        log.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        return validateTokenInternal(token);
    }

    // 리프레시 토큰 검증
    public boolean validateRefreshToken(String token) {
        return validateTokenInternal(token);
    }

    // 토큰 검증 공통 로직
    private boolean validateTokenInternal(String token) {
        if (isTokenBlacklisted(token)) {
            throw new CustomException(ErrorType.LOGGED_OUT_TOKEN);
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature", e);
            throw new CustomException(ErrorType.INVALID_JWT);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
            throw new CustomException(ErrorType.INVALID_JWT);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty", e);
            throw new CustomException(ErrorType.INVALID_JWT);
        } catch (Exception e) {
            log.error("JWT validation failed", e);
            throw new CustomException(ErrorType.INVALID_JWT);
        }
        return false;
    }

    // JWT access token 에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 요청에서 토큰 추출
    private String getTokenFromRequest(HttpServletRequest req, String header) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(header))
                .map(cookie -> {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.error("Error decoding cookie", e);
                        return null;
                    }
                })
                .findFirst()
                .orElse(null);
    }

    // AccessToken 가져오기
    public String getAccessTokenFromRequest(HttpServletRequest req) {
        return getTokenFromRequest(req, AUTHORIZATION_HEADER);
    }

    // RefreshToken 가져오기
    public String getRefreshTokenFromRequest(HttpServletRequest req) {
        return getTokenFromRequest(req, REFRESH_HEADER);
    }

    // 토큰 블랙리스트 검사
    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    public void addBlackListToken(String token) {
        tokenBlacklist.add(token);
    }
}
