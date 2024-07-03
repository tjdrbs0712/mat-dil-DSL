package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Comment;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepositoryCustom {
    Page<Comment> findLikedCommentsByUser(User user, Pageable pageable);
}
