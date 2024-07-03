package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Restaurant;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RestaurantRepositoryCustom {
    Page<Restaurant> getRestaurantLikeUserList(List<User> users, Pageable pageable);
    Page<Restaurant> findLikedRestaurantsByUser(User user, Pageable pageable);
}
