package com.projekt.projekt.Models.Ecommerce;

import com.projekt.projekt.Enums.DELIVERY;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class Delivery {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotNull(message = "Please set price")
    private BigDecimal price = new BigDecimal(0);
    @Column(unique = true,updatable = false)
    @NotNull(message = "Please set title!")
    @Enumerated(EnumType.STRING)
    private DELIVERY title;

    public Delivery() {
    }

    public Delivery(BigDecimal price, DELIVERY title) {
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

    public DELIVERY getTitle() {
        return title;
    }

    public void setTitle(DELIVERY title) {
        this.title = title;
    }

}
