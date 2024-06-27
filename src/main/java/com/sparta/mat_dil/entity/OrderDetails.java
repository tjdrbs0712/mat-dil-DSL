package com.sparta.mat_dil.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_details")
public class OrderDetails extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OnDelete(action = OnDeleteAction.NO_ACTION)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false)
    private int price;

    public OrderDetails(Order order, Food food, int price) {
        this.order = order;
        this.food = food;
        this.price = price;
    }

    public void setOrder(Order order){
        this.order = order;
        if(order != null && order.getOrderDetailsList().contains(this)){
            order.getOrderDetailsList().add(this);
        }
    }
}

