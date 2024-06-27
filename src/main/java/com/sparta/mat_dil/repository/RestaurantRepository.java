package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("SELECT r FROM Restaurant r WHERE r.deleteStatus = 'ACTIVE'")
    Page<Restaurant> findAllActiveRestaurants(Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.deleteStatus = 'ACTIVE'")
    List<Restaurant> findAllActiveRestaurants();

    @Query("SELECT r FROM Restaurant r WHERE r.deleteStatus = 'ACTIVE' AND r.id = :id")
    Optional<Restaurant> findIdActiveRestaurant(@Param("id") Long id);

//    @Query("SELECT r FROM Restaurant r WHERE r.deleteStatus = 'ACTIVE' AND r.id = ?1")
//    List<Restaurant> findIdActiveRestaurants(Long id);
}
