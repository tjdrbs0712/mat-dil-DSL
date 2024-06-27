package com.sparta.mat_dil.service;

import com.sparta.mat_dil.dto.OrderDetailDataDto;
import com.sparta.mat_dil.dto.OrderResponseDto;
import com.sparta.mat_dil.entity.*;
import com.sparta.mat_dil.enums.ErrorType;
import com.sparta.mat_dil.enums.ResponseStatus;
import com.sparta.mat_dil.exception.CustomException;
import com.sparta.mat_dil.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final FoodRepository foodRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final UserRepository userRepository;


    @Transactional
    public OrderDetailDataDto<List<OrderResponseDto>> create(Long restaurantId, List<Long> foodIdList, User user) {
        validateUser(user);
        Restaurant restaurant = validateRestaurant(restaurantId);

        Order order = createOrder(user, restaurant);
        List<Food> foodList = fetchFoods(foodIdList);
        int totalSum = processOrderDetails(order, foodList);

        List<OrderResponseDto> orderResponseList = createOrderResponseList(foodList);
        return new OrderDetailDataDto<>(ResponseStatus.FOOD_CHECK_SUCCESS, orderResponseList, totalSum);
    }

    private Order createOrder(User user, Restaurant restaurant) {
        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .build();
        orderRepository.save(order);
        return order;
    }

    private List<Food> fetchFoods(List<Long> foodIdList) {
        List<Food> foodList = new ArrayList<>();
        for (Long foodId : foodIdList) {
            Food food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND_FOOD));
            foodList.add(food);
        }
        return foodList;
    }

    private int processOrderDetails(Order order, List<Food> foodList) {
        int sum = 0;
        for (Food food : foodList) {
            OrderDetails orderDetails = new OrderDetails(order, food, food.getPrice());
            sum += food.getPrice();
            order.sumPrice(food.getPrice());
            orderDetailsRepository.save(orderDetails);
        }
        return sum;
    }

    private List<OrderResponseDto> createOrderResponseList(List<Food> foodList) {
        List<OrderResponseDto> orderResponseList = new ArrayList<>();
        for (Food food : foodList) {
            orderResponseList.add(new OrderResponseDto(food));
        }
        return orderResponseList;
    }

    /**
     * 유저 검증
     * @param user 로그인 유저
     */
    public void validateUser(User user){
        userRepository.findById(user.getId()).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_USER));

        if(user.getUserStatus().equals(UserStatus.DEACTIVATE)){
            throw new CustomException(ErrorType.DEACTIVATE_USER);
        }

        if(user.getUserStatus().equals(UserStatus.BLOCKED)){
            throw new CustomException(ErrorType.BLOCKED_USER);
        }
    }

    /**
     * 레스토랑 검증
     * @param restaurantId 레스토랑 id
     */
    public Restaurant validateRestaurant(Long restaurantId){
        return restaurantRepository.findById(restaurantId).orElseThrow(() ->
                new CustomException(ErrorType.NOT_FOUND_RESTAURANT));
    }


}
