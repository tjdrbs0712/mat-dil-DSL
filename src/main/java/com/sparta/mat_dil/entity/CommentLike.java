package com.sparta.mat_dil.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class CommentLike extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(nullable = false)
    private boolean Liked = true;

    @Builder
    public CommentLike(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }

    public void updateLike() {
        this.Liked = !this.Liked;
    }

    public void setComment(Comment comment){
        this.comment = comment;
        if(comment != null && !comment.getCommentLikes().contains(this)){
            comment.getCommentLikes().add(this);
        }
    }
}
