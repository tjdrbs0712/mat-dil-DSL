package com.sparta.mat_dil.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class RestaurantLike extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private boolean Liked = true;

    @Builder
    public RestaurantLike(User user, Restaurant restaurant) {
        this.user = user;
        this.restaurant = restaurant;
    }

    public void updateLike() {
        this.Liked = !this.Liked;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        if (restaurant != null && !restaurant.getRestaurantLikes().contains(this)) {
            restaurant.getRestaurantLikes().add(this);
        }
    }
}
