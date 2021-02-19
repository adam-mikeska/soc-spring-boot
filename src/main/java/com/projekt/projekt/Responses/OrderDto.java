package com.projekt.projekt.Responses;

import com.projekt.projekt.Enums.OrderState;
import com.projekt.projekt.Models.Ecommerce.Cart_OrderDelivery;
import com.projekt.projekt.Models.Ecommerce.Cart_OrderPayment;
import com.projekt.projekt.Models.Ecommerce.Order;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class OrderDto {
    @Id
    private Integer id;
    @NotBlank(message = "Please enter name!")
    private String name;
    @NotBlank(message = "Please enter email!")
    private String email;
    @Enumerated(EnumType.STRING)
    private OrderState orderState;
    private LocalDateTime created;
    private BigDecimal totalPrice;
    private Cart_OrderPayment cart_orderPayment;
    private Cart_OrderDelivery cart_orderDelivery;

    public OrderDto(Order order) {
        this.orderState=order.getOrderState();
        this.id=order.getId();
        this.created=order.getCreated();
        this.totalPrice=order.getTotalPrice();
        this.cart_orderPayment=order.getCart_orderPayment();
        this.cart_orderDelivery=order.getCart_orderDelivery();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Cart_OrderPayment getCart_orderPayment() {
        return cart_orderPayment;
    }

    public void setCart_orderPayment(Cart_OrderPayment cart_orderPayment) {
        this.cart_orderPayment = cart_orderPayment;
    }

    public Cart_OrderDelivery getCart_orderDelivery() {
        return cart_orderDelivery;
    }

    public void setCart_orderDelivery(Cart_OrderDelivery cart_orderDelivery) {
        this.cart_orderDelivery = cart_orderDelivery;
    }

}
