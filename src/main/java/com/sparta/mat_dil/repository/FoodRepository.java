package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    @Query("SELECT f FROM Food f WHERE f.id = :id AND f.restaurant.id = :restaurantId AND f.deleteStatus = 'ACTIVE'")
    Optional<Food> findByIdAndRestaurant_Id(@Param("id") Long id, @Param("restaurantId") Long restaurantId);

    @Query("SELECT f FROM Food f WHERE f.restaurant.id = :restaurantId AND f.deleteStatus = 'ACTIVE'")
    Page<Food> findByRestaurantId(@Param("restaurantId") Long restaurantId, Pageable pageable);

    @Query("SELECT f FROM Food f WHERE f.deleteStatus = 'ACTIVE' AND f.id = :id")
    Optional<Food> findByIdAndActive(@Param("id") Long id);
}
