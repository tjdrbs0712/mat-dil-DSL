package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.*;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.jwt.JwtUtil;
import com.sparta.mat_dil.repository.*;
import com.sparta.mat_dil.util.PageUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "유저 서비스")
public class UserService extends PageUtil {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CommentRepository commentRepository;
    private final RestaurantLikeRepository restaurantLikeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final FollowRepository followRepository;
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

    public ProfileResponseDto getProfile(User user) {
        validateUser(user);
        Long restaurantLike = restaurantLikeRepository.countByUserAndLike(user);
        Long commentLike = commentLikeRepository.countByUserAndLike(user);
        ProfileResponseDto profileResponseDto = new ProfileResponseDto(findById(user.getId()));
        profileResponseDto.updateLike(restaurantLike, commentLike);
        return profileResponseDto;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorType.NOT_FOUND_USER)
        );
    }

    /**
     * 음식점 좋아요 목록 조회
     * @param page 조회할 페이지 번호
     * @param user 로그인 유저
     * @return 음식점 목록을 날짜순으로 정렬해서 반환
     */
    public Page<RestaurantResponseDto> getLikeRestaurants(int page, User user) {
        validateUser(user);
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Restaurant> restaurants = restaurantRepository.findLikedRestaurantsByUser(user, pageable);
        return restaurants.map(RestaurantResponseDto::new);
    }

    /**
     * 댓글 좋아요 목록 조회
     * @param page 조회할 페이지 번호
     * @param user 로그인 유저
     * @return 댓글 목록을 날짜순으로 정렬해서 반환
     */
    public Page<CommentResponseDto> getLikeComments(int page, User user) {
        validateUser(user);
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Comment> comments = commentRepository.findLikedCommentsByUser(user, pageable);
        return comments.map(CommentResponseDto::new);
    }

    /**
     * 팔로우 등록
     * @param user 로그인한 유저
     * @param followingId 팔로윙 유저
     */
    @Transactional
    public void followUser(User user, Long followingId){
        User follower = userRepository.findById(user.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));

        validateUser(follower);
        User following = userRepository.findById(followingId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));
        validateUser(following);
        if(follower.getId().equals(following.getId())){
            throw new CustomException(ErrorType.DUPLICATE_USER);
        }

        Optional<Follow> findFollow = followRepository.findByFollowerAndFollowing(follower, following);

        if(findFollow.isPresent()) {
            throw new CustomException(ErrorType.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        follower.getFollowing().add(follow);
        followRepository.save(follow);

    }

    //팔로우 삭제
    @Transactional
    public void deleteFollowUser(User user, Long followingId) {
        User follower = userRepository.findById(user.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));
        validateUser(follower);
        User following = userRepository.findById(followingId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));
        validateUser(following);

        Optional<Follow> findFollow = followRepository.findByFollowerAndFollowing(follower, following);

        if(findFollow.isEmpty()){
            throw new CustomException(ErrorType.NOT_FOUND_FOLLOW);
        }

        follower.getFollowing().remove(findFollow.get());
        followRepository.delete(findFollow.get());
    }

    //팔로우 조회
    @Transactional(readOnly = true)
    public Page<RestaurantResponseDto> getFollowRestaurants(User user, int page, String sortBy) {
        User follower = userRepository.findById(user.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));
        validateUser(follower);

        Pageable pageable = createPageable(page, sortBy);

        List<User> followingList = follower.getFollowing().stream()
                .map(Follow::getFollowing).toList();
        Page<Restaurant> restaurantPage = restaurantRepository.getRestaurantLikeUserList(followingList, pageable);
        List<RestaurantResponseDto> responseDtoList = restaurantPage.getContent().stream()
                .map(RestaurantResponseDto::new).toList();

        return new PageImpl<>(responseDtoList, pageable, restaurantPage.getTotalElements());
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
