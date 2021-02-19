package com.projekt.projekt.Models.Ecommerce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.projekt.Enums.DELIVERY;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Cart_OrderDelivery {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private DELIVERY delivery;
    private BigDecimal price= new BigDecimal(0);
    private String cartId;
    @OneToOne
    @JsonIgnore
    private Delivery deliveryENT;

    public Cart_OrderDelivery() {
    }

    public Cart_OrderDelivery(DELIVERY delivery, BigDecimal price, String cartId,Delivery deliveryENT) {
        this.delivery = delivery;
        this.price = price;
        this.cartId = cartId;
        this.deliveryENT=deliveryENT;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DELIVERY getDelivery() {
        return delivery;
    }

    public void setDelivery(DELIVERY delivery) {
        this.delivery = delivery;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public Delivery getDeliveryENT() {
        return deliveryENT;
    }

    public void setDeliveryENT(Delivery deliveryENT) {
        this.deliveryENT = deliveryENT;
    }
}
