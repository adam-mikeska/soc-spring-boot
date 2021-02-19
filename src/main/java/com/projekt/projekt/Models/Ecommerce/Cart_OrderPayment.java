package com.projekt.projekt.Models.Ecommerce;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projekt.projekt.Enums.PAYMENT;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Cart_OrderPayment {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private PAYMENT payment;
    private BigDecimal price= new BigDecimal(0);;
    private String cartId;
    @OneToOne
    @JsonIgnore
    private Payment paymentENT;

    public Cart_OrderPayment() {
    }

    public Cart_OrderPayment(PAYMENT payment, BigDecimal price, String cartId,Payment paymentENT) {
        this.payment = payment;
        this.price = price;
        this.cartId = cartId;
        this.paymentENT=paymentENT;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PAYMENT getPayment() {
        return payment;
    }

    public void setPayment(PAYMENT payment) {
        this.payment = payment;
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

    public Payment getPaymentENT() {
        return paymentENT;
    }

    public void setPaymentENT(Payment paymentENT) {
        this.paymentENT = paymentENT;
    }
}
