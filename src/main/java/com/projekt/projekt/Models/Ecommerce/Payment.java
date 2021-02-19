package com.projekt.projekt.Models.Ecommerce;

import com.projekt.projekt.Enums.PAYMENT;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class Payment {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "Please set price")
    private BigDecimal price = new BigDecimal(0);
    @Column(unique = true,updatable = false)
    @Enumerated(EnumType.STRING)
    private PAYMENT title;

    public Payment() {
    }

    public Payment(BigDecimal price, PAYMENT title) {
        this.price = price;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PAYMENT getTitle() {
        return title;
    }

    public void setTitle(PAYMENT title) {
        this.title = title;
    }
}
