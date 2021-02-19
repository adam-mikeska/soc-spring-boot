package com.projekt.projekt.Responses;

import com.projekt.projekt.Models.Ecommerce.Brand;
import com.projekt.projekt.Models.Ecommerce.Category;
import com.projekt.projekt.Models.Ecommerce.Product;
import lombok.Data;

import javax.persistence.Id;

@Data
public class ProductDto {
    private String title;
    private Category category;
    private Brand brand;

    public ProductDto(Product product) {
        this.title=product.getTitle();
        this.category=product.getCategory();
        this.brand=product.getBrand();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }
}
