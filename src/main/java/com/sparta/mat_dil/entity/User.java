package com.sparta.mat_dil.entity;

import com.sparta.mat_dil.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String intro;

    @Column
    private String refreshToken;

    @Column
    @Enumerated(value = EnumType.STRING)
    private UserType userType;

    @Column
    @Enumerated(value = EnumType.STRING)
    private UserStatus userStatus;

    @Column
    private Long kakaoId;

    @OneToMany(mappedBy = "follower")
    private Set<Follow> following = new HashSet<>();

    //로그인시 리프레시 토큰 초기화
    @Transactional
    public void refreshTokenReset(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void update(Optional<String> newPassword, Optional<String> name, Optional<String> intro) {
        this.password = newPassword.orElse(this.password);
        this.name = name.orElse(this.name);
        this.intro = intro.orElse(this.intro);
    }

    public User(UserRequestDto requestDto) {
        this.accountId = requestDto.getAccountId();
        this.password = requestDto.getPassword();
        this.name = requestDto.getName();
        this.email = requestDto.getEmail();
        this.userType = requestDto.getUserType();
        this.userStatus = UserStatus.ACTIVE;
    }

    public User(String accountId, String password, String name, String email, UserType userType, UserStatus userStatus, Long kakaoId) {
        this.accountId = accountId;
        this.password=password;
        this.name=name;
        this.email=email;
        this.userType=userType;
        this.userStatus=userStatus;
        this.kakaoId=kakaoId;
    }

    public void withdrawUser() {
        this.userStatus = UserStatus.DEACTIVATE;
    }

    public void logout(){
        this.refreshToken=null;
    }

    public void kakaoIdUpdate(Long kakaoId) {
        this.kakaoId=kakaoId;
    }
}
