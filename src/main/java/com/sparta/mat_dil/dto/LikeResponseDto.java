package com.sparta.mat_dil.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class LikeResponseDto {

    private String name;
    private String contents;
    private Long likeCount;
    private Boolean isLike;

    @Builder
    public LikeResponseDto(String name, String contents, Long likeCount, Boolean isLike){
        this.name = name;
        this.contents = contents;
        this.likeCount = likeCount;
        this.isLike = isLike;
    }
}
