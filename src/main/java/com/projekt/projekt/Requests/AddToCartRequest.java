package com.projekt.projekt.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddToCartRequest {
    @NotBlank(message = "Please set product!")
    private String product;
    @NotBlank(message = "Please set product!")
    private String underTitle;
    @NotBlank(message = "Please set size!")
    private String size;
    @NotNull(message = "Please set quantity!")
    @Min(value = 1,message = "Minimum quantity is 1")
    private Integer quantity;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getUnderTitle() {
        return underTitle;
    }

    public void setUnderTitle(String underTitle) {
        this.underTitle = underTitle;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
