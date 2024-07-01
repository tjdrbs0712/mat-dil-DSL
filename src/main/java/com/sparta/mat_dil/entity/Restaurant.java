package com.sparta.mat_dil.entity;

import com.sparta.mat_dil.dto.RestaurantRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DialectOverride;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeleteStatus deleteStatus = DeleteStatus.ACTIVE;

    public Restaurant(User loginUser, RestaurantRequestDto requestDto) {

        this.user = loginUser;
        this.restaurantName = requestDto.getRestaurantName();
        this.description = requestDto.getDescription();
    }

    public void update(RestaurantRequestDto requestDto) {
        this.restaurantName = requestDto.getRestaurantName();
        this.description = requestDto.getDescription();
    }

    public void updateLike(boolean isLike) {
        if (isLike) {
            this.likeCount += 1;
        } else {
            this.likeCount -= 1;
        }
    }

    public void softDelete() {
        this.deleteStatus = DeleteStatus.DELETED;
    }

    public void restore() {
        this.deleteStatus = DeleteStatus.ACTIVE;
    }
}