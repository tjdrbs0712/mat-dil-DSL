package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Comment;
import com.sparta.mat_dil.entity.CommentLike;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    @Query("select count(c) from CommentLike c where c.user = :user and c.liked = true")
    Long countByUserAndLike(@Param("user") User user);
}