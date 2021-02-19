package com.projekt.projekt.Models.Ecommerce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Models.User;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.*;

@Entity
public class Cart {

    @Id
    @Column(unique = true, nullable = false)
    private String id = UUID.randomUUID().toString();
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();
    @OneToOne
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @OneToOne
    private Cart_OrderDelivery cart_orderDelivery;
    @OneToOne
    private Cart_OrderPayment cart_orderPayment;
    @OneToOne
    private Coupon coupon;

    public Cart() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Cart_OrderDelivery getCart_orderDelivery() {
        return cart_orderDelivery;
    }

    public void setCart_orderDelivery(Cart_OrderDelivery cart_orderDelivery) {
        this.cart_orderDelivery = cart_orderDelivery;
    }

    public Cart_OrderPayment getCart_orderPayment() {
        return cart_orderPayment;
    }

    public void setCart_orderPayment(Cart_OrderPayment cart_orderPayment) {
        this.cart_orderPayment = cart_orderPayment;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCart(this);
        checkForCartPrice();
    }

    public void removeCartItem(CartItem cartItem) {
        cartItems.remove(cartItem);
        cartItem.setCart(null);
        checkForCartPrice();
    }

    public void checkForCartPrice(){
        if (this.coupon != null) {
            BigDecimal totalPriceWithoutPaymentAndDelivery = this.cartItems.stream().reduce(BigDecimal.ZERO, (bd, item) -> bd.add((item.getProductOption().getPrice().subtract(item.getProductOption().getDiscount())).multiply(BigDecimal.valueOf(item.getQuantity()))), BigDecimal::add);

            if (this.coupon.getDiscountType().equals(DISCOUNT.PERCENTAGE)) {
                if (totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getMinCartPrice()) == -1) {
                    this.coupon = null;
                }

            } else if (this.coupon.getDiscountType().equals(DISCOUNT.TOTAL)) {
                if (totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getMinCartPrice()) == -1 || totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getDiscount()) != 1) {
                    this.coupon = null;
                }
            } else if (this.coupon.getDiscountType().equals(DISCOUNT.DELIVERY)) {
                if (totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getMinCartPrice()) == -1) {
                    this.getCart_orderDelivery().setPrice(this.cart_orderDelivery.getDeliveryENT().getPrice());
                    this.coupon = null;
                }
            }
        }
    }

    public void removeAll() {
        cartItems.clear();
    }


}
