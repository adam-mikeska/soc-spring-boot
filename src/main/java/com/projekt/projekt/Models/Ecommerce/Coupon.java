package com.projekt.projekt.Models.Ecommerce;

import com.projekt.projekt.Enums.DISCOUNT;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class Coupon {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Please set code!")
    @Column(updatable = false,unique = true,nullable = false)
    private String code;
    private BigDecimal discount = new BigDecimal(0);
    @NotNull(message = "Set min car price!")
    private BigDecimal minCartPrice= new BigDecimal(0);
    @NotNull(message = "Please set discount type!")
    @Enumerated(value = EnumType.STRING)
    private DISCOUNT discountType;
    private Boolean enabled = true;
    public Coupon() {
    }

    public Coupon(String code, BigDecimal discount, DISCOUNT discountType, BigDecimal minCartPrice) {
        this.code = code;
        this.discount = discount;
        this.discountType = discountType;
        this.minCartPrice=minCartPrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public DISCOUNT getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DISCOUNT discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getMinCartPrice() {
        return minCartPrice;
    }

    public void setMinCartPrice(BigDecimal minCartPrice) {
        this.minCartPrice = minCartPrice;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
