package com.sparta.mat_dil.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.mat_dil.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j(topic = "레스토랑 레파지토리")
public class RestaurantRepositoryImpl implements RestaurantRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Restaurant> getRestaurantLikeUserList(List<User> users, Pageable pageable) {
        QRestaurant restaurant = QRestaurant.restaurant;
        JPAQuery<Restaurant> query = jpaQueryFactory.selectFrom(restaurant)
                .where(restaurant.user.in(users))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Restaurant> pathBuilder = new PathBuilder<>(restaurant.getType(), restaurant.getMetadata());
            query.orderBy(new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.get(order.getProperty(), Comparable.class)
            ));
        }

        // 전체 결과 수 계산
        Long total = jpaQueryFactory.select(restaurant.count())
                .from(restaurant)
                .where(restaurant.user.in(users))
                .fetchOne();

        // 결과 페치
        List<Restaurant> results = query.fetch();
        return new PageImpl<>(results, pageable, Optional.ofNullable(total).orElse(0L));
    }

    @Override
    public Page<Restaurant> findLikedRestaurantsByUser(User user, Pageable pageable) {
        QRestaurant restaurant = QRestaurant.restaurant;
        QRestaurantLike restaurantLike = QRestaurantLike.restaurantLike;
        JPAQuery<Restaurant> query = jpaQueryFactory.selectFrom(restaurant)
                .join(restaurantLike).on(restaurantLike.restaurant.eq(restaurant))
                .where(restaurantLike.user.eq(user)
                        .and(restaurantLike.liked.eq(true))
                        .and(restaurant.deleteStatus.eq(DeleteStatus.ACTIVE)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(restaurant.createdAt.desc());

        // 전체 결과 수 계산
        Long total = jpaQueryFactory.select(restaurant.count())
                .from(restaurant)
                .join(restaurantLike).on(restaurantLike.restaurant.eq(restaurant))
                .where(restaurantLike.user.eq(user)
                        .and(restaurantLike.liked.eq(true))
                        .and(restaurant.deleteStatus.eq(DeleteStatus.ACTIVE)))
                .fetchOne();


        List<Restaurant> results = query.fetch();

        return new PageImpl<>(results, pageable, Optional.ofNullable(total).orElse(0L));
    }
}
