package com.projekt.projekt.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddOrUpdateProductRequest {
    @NotBlank(message = "Please enter title!")
    private String title;
    @NotBlank(message = "Please enter under title!")
    private String underTitle;
    @NotNull(message = "Please enter price!")
    @Min(value = 0,message = "Price has to be higher than 0!")
    private BigDecimal price;
    @Min(value = 0,message = "Discount has to be higher than 0!")
    private BigDecimal discount;
    @Column(unique = true)
    private Integer category;
    @Column(unique = true)
    private String brand;
    private Boolean enabled;
    @Column(unique = true)
    @NotBlank(message = "Please enter description!")
    private String description;
    private String productSizes;
    private MultipartFile[] images;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUnderTitle() {
        return underTitle;
    }

    public void setUnderTitle(String underTitle) {
        this.underTitle = underTitle;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile[] getImages() {
        return images;
    }

    public void setImages(MultipartFile[] images) {
        this.images = images;
    }

    public String getProductSizes() {
        return productSizes;
    }

    public void setProductSizes(String productSizes) {
        this.productSizes = productSizes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
