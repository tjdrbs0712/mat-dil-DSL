package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.LikeResponseDto;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final CommentRepository commentRepository;
    private final RestaurantLikeRepository restaurantLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    /**
     * 음식점 좋아요 등록 / 취소
     * @param restaurantId 음식점 id
     * @param user 로그인 유저
     * @return LikeResponseDto 음식점 이름, 소개, 좋아요 등록 or 취소 결과
     */
    @Transactional
    public LikeResponseDto updateRestaurantLike(Long restaurantId, User user) {

        validateUser(user);

        Restaurant restaurant = validateRestaurant(restaurantId);
        if(restaurant.getUser().getId().equals(user.getId())){
            throw new CustomException(ErrorType.CONTENT_OWNER);
        }

        RestaurantLike restaurantLike = restaurantLikeRepository.findByUserAndRestaurant(user, restaurant).orElse(null);
        if(restaurantLike != null){
            restaurantLike.updateLike();
        }
        else{
            restaurantLike = RestaurantLike.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .build();
            restaurantLikeRepository.save(restaurantLike);
        }

        restaurant.updateLike(restaurantLike.isLiked());

        return LikeResponseDto.builder()
                .name(restaurant.getRestaurantName())
                .contents(restaurant.getDescription())
                .isLike(restaurantLike.isLiked())
                .likeCount(restaurant.getLikeCount())
                .build();
    }

    /**
     * 댓글 좋아요 등록 / 취소
     * @param restaurantId 음식점 id
     * @param commentId 댓글 id
     * @param user 로그인 유저
     * @return LikeResponseDto 댓글 작성자 id, 댓글 내용, 좋아요 등록 or 취소 결과
     */
    @Transactional
    public LikeResponseDto updateCommentLike(Long restaurantId, Long commentId, User user) {
        validateUser(user);
        validateRestaurant(restaurantId);

        Comment comment = commentRepository.findById(commentId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_COMMENT));
        if(comment.getUser().getId().equals(user.getId())){
            throw new CustomException(ErrorType.CONTENT_OWNER);
        }

        CommentLike commentLike = commentLikeRepository.findByUserAndComment(user, comment).orElse(null);
        if (commentLike != null) {
            commentLike.updateLike();
        }
        else{
            commentLike = CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(commentLike);
        }

        comment.updateLike(commentLike.isLiked());

        return LikeResponseDto.builder()
                .name(comment.getUser().getAccountId())
                .contents(comment.getDescription())
                .isLike(commentLike.isLiked())
                .likeCount(comment.getLikeCount())
                .build();
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

    /**
     * 레스토랑 검증
     * @param restaurantId 레스토랑 id
     */
    public Restaurant validateRestaurant(Long restaurantId){
         return restaurantRepository.findById(restaurantId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_RESTAURANT));
    }
}



