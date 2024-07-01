package com.sparta.mat_dil.controller;

import com.sparta.mat_dil.dto.*;
import com.sparta.mat_dil.enums.ResponseStatus;
import com.sparta.mat_dil.security.UserDetailsImpl;
import com.sparta.mat_dil.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j(topic = "유저 컨트롤러")
public class UserController {

    private final UserService userService;

    //회원 가입
    @PostMapping
    public ResponseEntity<ResponseMessageDto> createUser(@Valid @RequestBody UserRequestDto requestDto) {

        userService.createUser(requestDto);
        return ResponseEntity.ok(new ResponseMessageDto(ResponseStatus.SIGN_UP_SUCCESS));
    }

    //회원 탈퇴
    @PatchMapping
    public ResponseEntity<ResponseMessageDto> withdrawUser(@Valid @RequestBody PasswordRequestDto requestDTO,
        @AuthenticationPrincipal UserDetailsImpl userDetails){
        userService.withdrawUser(requestDTO, userDetails.getUser());
        return ResponseEntity.ok(new ResponseMessageDto(ResponseStatus.DEACTIVATE_USER_SUCCESS));
    }

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ResponseMessageDto> logout(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletResponse res,
                                                     HttpServletRequest req){

        userService.logout(userDetails.getUser(), res, req);
        return ResponseEntity.ok(new ResponseMessageDto(ResponseStatus.LOGOUT_SUCCESS));
    }

    //회원 정보 조회
    @GetMapping
    public ResponseEntity<ResponseDataDto<ProfileResponseDto>> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(new ResponseDataDto<>(ResponseStatus.PROFILE_CHECK_SUCCESS, userService.getProfile(userDetails.getUser())));
    }

    //회원 정보 수정
    @PutMapping
    public ResponseEntity<ResponseDataDto<ProfileResponseDto>> profileUpdate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ProfileRequestDto requestDto) {
        ProfileResponseDto responseDto = userService.update(userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok(new ResponseDataDto<>(ResponseStatus.PROFILE_UPDATE_SUCCESS, responseDto));
    }

    @GetMapping("/restaurants/likes")
    public ResponseEntity<ResponseDataDto<Page<RestaurantResponseDto>>> getLikeRestaurants(@RequestParam(value = "page") int page,
                                                          @AuthenticationPrincipal UserDetailsImpl userDetails){
        Page<RestaurantResponseDto> responseDtoPage = userService.getLikeRestaurants(page - 1, userDetails.getUser());

        return ResponseEntity.ok(new ResponseDataDto<>(ResponseStatus.RESTAURANT_CHECK_SUCCESS, responseDtoPage));
    }

    @GetMapping("/comments/likes")
    public ResponseEntity<ResponseDataDto<Page<CommentResponseDto>>> getLikeComments(@RequestParam(value = "page") int page,
                                                                                           @AuthenticationPrincipal UserDetailsImpl userDetails){
        Page<CommentResponseDto> commentResponseDto = userService.getLikeComments(page - 1, userDetails.getUser());

        return ResponseEntity.ok(new ResponseDataDto<>(ResponseStatus.COMMENTS_CHECK_SUCCESS, commentResponseDto));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<ResponseMessageDto> followUser(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        userService.followUser(userDetails.getUser(), id);

        return ResponseEntity.ok(new ResponseMessageDto(ResponseStatus.FOLLOW_SUCCESS));
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<ResponseMessageDto> deleteFollowUser(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        userService.deleteFollowUser(userDetails.getUser(), id);

        return ResponseEntity.ok(new ResponseMessageDto(ResponseStatus.UNFOLLOW_SUCCESS));
    }

}
