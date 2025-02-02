package com.sparta.mat_dil.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FoodRequestDto {
    private String foodName;
    private int price;
    private String description;
}
