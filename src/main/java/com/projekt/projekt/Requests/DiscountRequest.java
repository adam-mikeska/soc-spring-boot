package com.projekt.projekt.Requests;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class DiscountRequest {
    public enum DicsountBy {
        Brand,Category,Product
    }
    @NotNull(message = "Please set by which field you would like to discount products!")
    private DicsountBy discountBy;
    @NotNull(message = "Please set discount!")
    @Max(value = 100,message = "Enter valid percentual discount!")
    private BigDecimal discount;
    @NotBlank(message = "Please set value!")
    private String value;

    public DicsountBy getDiscountBy() {
        return discountBy;
    }

    public void setDiscountBy(DicsountBy discountBy) {
        this.discountBy = discountBy;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
