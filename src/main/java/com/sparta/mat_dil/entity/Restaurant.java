package com.sparta.mat_dil.entity;

import com.sparta.mat_dil.dto.RestaurantRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "restaurant")
public class Restaurant extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column
    private Boolean pinned;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantLike> restaurantLikes = new ArrayList<>();

    public Restaurant(User loginUser, RestaurantRequestDto requestDto) {
        this.user = loginUser;
        this.restaurantName = requestDto.getRestaurantName();
        this.description = requestDto.getDescription();
    }

    public void update(RestaurantRequestDto requestDto) {
        this.restaurantName = requestDto.getRestaurantName();
        this.description = requestDto.getDescription();
    }

    public Long updateLike(boolean islike){
        if(islike){this.likeCount += 1;}
        else{this.likeCount -= 1;}
        return this.likeCount;
    }

    public void addRestaurantLike(RestaurantLike restaurantLike) {
        this.restaurantLikes.add(restaurantLike);
        restaurantLike.setRestaurant(this);
    }
}