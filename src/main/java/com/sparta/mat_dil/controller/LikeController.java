package com.sparta.mat_dil.controller;

import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.dto.ResponseDataDto;
import com.sparta.mat_dil.enums.ResponseStatus;
import com.sparta.mat_dil.security.UserDetailsImpl;
import com.sparta.mat_dil.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restaurants/{restaurantId}")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * 레스토랑 좋아요
     * @param userDetails 로그인한 유저의 세부 정보
     * @param restaurantId 레스토랑 id
     *@return ResponseEntity<ResponseDataDto<LikeResponseDto>> 형태의 HTTP 응답
     *      - 상대 코드
     *      - 메시지
     *      - 데이터
     */
    @PutMapping("/like")
    public ResponseEntity<ResponseDataDto<LikeResponseDto>> updateRestaurantLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                 @PathVariable Long restaurantId){
        LikeResponseDto likeResponseDto = likeService.updateRestaurantLike(restaurantId, userDetails.getUser());

        ResponseStatus responseStatus = likeResponseDto.getIsLike() ? ResponseStatus.LIKE_CREATE_SUCCESS : ResponseStatus.LIKE_DELETE_SUCCESS;

        return ResponseEntity.ok(new ResponseDataDto<>(responseStatus, likeResponseDto));
    }

    /**
     * 댓글 좋아요
     * @param userDetails 로그인한 유저의 세부 정보
     * @param restaurantId 레스토랑 id
     * @param commentId 댓글 id
     * @return ResponseEntity<ResponseDataDto<LikeResponseDto>> 형태의 HTTP 응답
     *      - 상대 코드
     *      - 메시지
     *      - 데이터
     */
    @PutMapping("/comment/{commentId}/like")
    public ResponseEntity<ResponseDataDto<LikeResponseDto>> updateCommentLike(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                              @PathVariable Long restaurantId, @PathVariable Long commentId){
        LikeResponseDto likeResponseDto = likeService.updateCommentLike(restaurantId, commentId, userDetails.getUser());

        ResponseStatus responseStatus = likeResponseDto.getIsLike() ? ResponseStatus.LIKE_CREATE_SUCCESS : ResponseStatus.LIKE_DELETE_SUCCESS;

        return ResponseEntity.ok(new ResponseDataDto<>(responseStatus, likeResponseDto));
    }


}
