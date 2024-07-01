package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.*;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.jwt.JwtUtil;
import com.sparta.mat_dil.repository.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "유저 서비스")
public class UserService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CommentRepository commentRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    //회원가입
    @Transactional
    public void createUser(UserRequestDto requestDto) {
        //동일 아이디 검증
        validateUserId(requestDto.getAccountId());

        //동일 이메일 검증
        validateUserEmail(requestDto.getEmail());

        //비밀번호 암호화
        String password = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(password);
        userRepository.save(new User(requestDto));

    }

    //회원 탈퇴
    @Transactional
    public void withdrawUser(PasswordRequestDto requestDTO, User curruntUser) {

        User user = userRepository.findByAccountId(curruntUser.getAccountId()).orElse(null);
        if (user == null) {
            throw new CustomException(ErrorType.NOT_FOUND_USER);
        }
        //회원 상태 확인
        checkUserType(curruntUser.getUserStatus());

//        //비밀번호 일치 확인
        if (!passwordEncoder.matches(requestDTO.getPassword(), curruntUser.getPassword())) {
            throw new CustomException(ErrorType.INVALID_PASSWORD);
        }

        //회원 상태 변경
        user.withdrawUser();
    }

    //동일 이메일 검증
    private void validateUserEmail(String email) {
        Optional<User> findUser = userRepository.findByEmail(email);

        if (findUser.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATE_EMAIL);
        }
    }

    //동일 아이디 검증
    private void validateUserId(String id) {
        Optional<User> findUser = userRepository.findByAccountId(id);

        if (findUser.isPresent()) {
            throw new CustomException(ErrorType.DUPLICATE_ACCOUNT_ID);
        }
    }

    private void checkUserType(UserStatus userStatus) {
        if (userStatus.equals(UserStatus.DEACTIVATE)) {
            throw new CustomException(ErrorType.DEACTIVATE_USER);
        }
    }

    //회원 정보 수정
    @Transactional
    public ProfileResponseDto update(Long userId, ProfileRequestDto requestDto) {
        User user = findById(userId);

        String newEncodePassword = updatePasswordIfNeeded(user, requestDto);

        user.update(
                Optional.ofNullable(newEncodePassword),
                Optional.ofNullable(requestDto.getName()),
                Optional.ofNullable(requestDto.getIntro())
        );

        return new ProfileResponseDto(user);
    }

    private String updatePasswordIfNeeded(User user, ProfileRequestDto requestDto) {
        if (requestDto.getPassword() == null) {
            return null;
        }

        validateCurrentPassword(user, requestDto.getPassword());
        validateNewPassword(requestDto.getPassword(), requestDto.getNewPassword(), user);

        String newEncodePassword = passwordEncoder.encode(requestDto.getNewPassword());

        savePasswordHistory(user, newEncodePassword);

        return newEncodePassword;
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new CustomException(ErrorType.INVALID_PASSWORD);
        }
    }

    private void validateNewPassword(String currentPassword, String newPassword, User user) {
        if (currentPassword.equals(newPassword)) {
            throw new CustomException(ErrorType.PASSWORD_RECENTLY_USED);
        }

        List<PasswordHistory> recentPasswords = passwordHistoryRepository.findTop3ByUserOrderByChangeDateDesc(user);
        boolean isInPreviousPasswords = recentPasswords.stream()
                .anyMatch(pw -> passwordEncoder.matches(newPassword, pw.getPassword()));
        if (isInPreviousPasswords) {
            throw new CustomException(ErrorType.PASSWORD_RECENTLY_USED);
        }
    }

    private void savePasswordHistory(User user, String newEncodePassword) {
        PasswordHistory passwordHistory = new PasswordHistory(user, newEncodePassword);
        passwordHistoryRepository.save(passwordHistory);
    }


    @Transactional
    public void logout(User user, HttpServletResponse res, HttpServletRequest req) {
        user.logout();
        Cookie[] cookies = req.getCookies();
        String accessToken=jwtUtil.getAccessTokenFromRequest(req);
        jwtUtil.addBlackListToken(accessToken.substring(7));

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue(null);
                cookie.setPath("/");
                cookie.setMaxAge(0);

                res.addCookie(cookie);
            }
        }


    }

    public ProfileResponseDto getProfile(Long userId) {
        return new ProfileResponseDto(findById(userId));
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_USER)
        );
    }


    public Page<RestaurantResponseDto> getLikeRestaurants(int page, User user) {
        validateUser(user);
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Restaurant> restaurants = restaurantRepository.findLikedRestaurantsByUser(user, pageable);
        return restaurants.map(RestaurantResponseDto::new);
    }

    public Page<CommentResponseDto> getLikeComments(int page, User user) {
        validateUser(user);
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Comment> comments = commentRepository.findLikedCommentsByUser(user, pageable);
        return comments.map(CommentResponseDto::new);
    }

    /**
     * 유저 검증
     * @param user 로그인 유저
     */
    public void validateUser(User user){
        userRepository.findById(user.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));

        if(user.getUserStatus().equals(UserStatus.DEACTIVATE)){
            throw new CustomException(ErrorType.DEACTIVATE_USER);
        }

        if(user.getUserStatus().equals(UserStatus.BLOCKED)){
            throw new CustomException(ErrorType.BLOCKED_USER);
        }
    }

}
