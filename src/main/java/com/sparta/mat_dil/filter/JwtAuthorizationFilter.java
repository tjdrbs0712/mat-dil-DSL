package com.sparta.mat_dil.filter;

import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.jwt.JwtUtil;
import com.sparta.mat_dil.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessTokenFromRequest(req);
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(req);
        if (StringUtils.hasText(accessToken)) {
            accessToken = jwtUtil.substringToken(accessToken);

            if (jwtUtil.validateToken(accessToken)) {
                setAuthenticationFromToken(accessToken);
            } else if (StringUtils.hasText(refreshToken)) {
                handleRefreshToken(refreshToken, res);
            }
        }

        filterChain.doFilter(req, res);
    }

    private void setAuthenticationFromToken(String token) {
        Claims claims = jwtUtil.getUserInfoFromToken(token);
        try {
            setAuthentication(claims.getSubject());
        } catch (Exception e) {
            throw new CustomException(ErrorType.NOT_FOUND_AUTHENTICATION_INFO);
        }
    }

    private void handleRefreshToken(String refreshToken, HttpServletResponse res) {
        refreshToken = jwtUtil.substringToken(refreshToken);
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            String newAccessToken = jwtUtil.generateAccessToken(refreshToken); // 새로운 access token 생성
            jwtUtil.addJwtToCookie(newAccessToken, JwtUtil.BEARER_PREFIX + refreshToken, res);
            setAuthenticationFromToken(jwtUtil.substringToken(newAccessToken)); // 새로운 access token으로 인증 설정
            log.info("새로운 토큰 생성 완료!!");
        } else {
            throw new CustomException(ErrorType.EXPIRED_JWT);
        }
    }

    // 인증 처리
    private void setAuthentication(String username) {
        log.debug("Authenticating user: {}", username);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}