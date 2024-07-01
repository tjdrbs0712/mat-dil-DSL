package com.sparta.mat_dil.dto;

import com.sparta.mat_dil.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private final String accountId;
    private final String name;
    private final String description;
    private final Long likeCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public CommentResponseDto(Comment comment){
        this.accountId = comment.getUser().getAccountId();
        this.name = comment.getUser().getName();
        this.description = comment.getDescription();
        this.likeCount = comment.getLikeCount();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }
}
