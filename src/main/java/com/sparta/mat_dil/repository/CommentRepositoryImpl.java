package com.sparta.mat_dil.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Comment> findLikedCommentsByUser(User user, Pageable pageable) {
        QComment comment = QComment.comment;
        QCommentLike commentLike = QCommentLike.commentLike;

        JPAQuery<Comment> query = jpaQueryFactory.selectFrom(comment)
                .join(commentLike).on(commentLike.comment.eq(comment))
                .where(commentLike.user.eq(user)
                        .and(commentLike.liked.eq(true)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(comment.createdAt.desc());

        Long total = jpaQueryFactory.select(comment.count())
                .from(comment)
                .join(commentLike).on(commentLike.comment.eq(comment))
                .where(commentLike.user.eq(user)
                        .and(commentLike.liked.eq(true)))
                .fetchOne();

        List<Comment> results = query.fetch();
        return new PageImpl<>(results, pageable, Optional.ofNullable(total).orElse(0L));
    }
}
