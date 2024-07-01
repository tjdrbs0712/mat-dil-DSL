package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.RestaurantLike;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantLikeRepository extends JpaRepository<RestaurantLike, Long> {
    Optional<RestaurantLike> findByUserAndRestaurant(User user, Restaurant restaurant);

    @Query("select count(r) from RestaurantLike r where r.user = :user and r.liked = true")
    Long countByUserAndLike(@Param("user") User user);
}