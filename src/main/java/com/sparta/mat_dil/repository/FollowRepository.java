package com.sparta.mat_dil.repository;

import com.sparta.mat_dil.entity.Follow;
import com.sparta.mat_dil.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    List<Follow> findAllByFollower(User follower);

}
