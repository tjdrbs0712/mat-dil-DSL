package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Comment;
import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    List<Comment> findAllByRestaurant(Restaurant restaurant);


//    @Query("SELECT c FROM Comment c JOIN CommentLike cl ON c.id = cl.comment.id WHERE cl.user = :user AND cl.liked = true")
//    Page<Comment> findLikedCommentsByUser(@Param("user") User user, Pageable pageable);

}
