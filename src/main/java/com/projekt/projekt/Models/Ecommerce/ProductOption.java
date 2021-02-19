package com.projekt.projekt.Models.Ecommerce;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.projekt.projekt.Requests.AddOrUpdateProductRequest;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class ProductOption {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Product product;
    @OneToMany
    private List<ProductImage> productImages = new ArrayList<>();
    @OneToMany
    private List<ProductSize> productSizes = new ArrayList<>();
    private String underTitle;
    @NotNull(message = "Please enter price")
    private BigDecimal price = new BigDecimal(0);
    @NotNull(message = "Please enter discount")
    private BigDecimal discount = new BigDecimal(0);
    @Length(max = 500000000, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter description")
    private String description;
    @JsonFormat( pattern = "dd/MM/yy HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime addedInDate = LocalDateTime.now();
    private Integer soldTimes = 0;
    private Boolean enabled = true;

    public ProductOption() {
    }


    public ProductOption(Product product, BigDecimal price, BigDecimal discount, String underTitle, String description) {
        this.product = product;
        this.price = price;
        this.discount = discount;
        this.underTitle = underTitle;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ProductImage> getProductImages() {
        return productImages;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getAddedInDate() {
        return addedInDate;
    }

    public void setAddedInDate(LocalDateTime addedInDate) {
        this.addedInDate = addedInDate;
    }

    public Integer getSoldTimes() {
        return soldTimes;
    }

    public void setSoldTimes(Integer soldTimes) {
        this.soldTimes = soldTimes;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<ProductSize> getProductSizes() {
        return productSizes;
    }

    public void setProductImages(List<ProductImage> productImages) {
        this.productImages = productImages;
    }

    public void addProductImage(ProductImage productImage) {
        productImages.add(productImage);
        productImage.setProductOption(this);
    }

    public void removeProductImage(ProductImage productImage) {
        productImages.remove(productImage);
        productImage.setProductOption(null);
    }

    public void setProductSizes(List<ProductSize> productSizes) {
        this.productSizes = productSizes;
    }

    public void addProductSize(ProductSize productSize) {
        productSizes.add(productSize);
        productSize.setProductOption(this);
    }

    public void removeProductSize(ProductSize productSize) {
        productSizes.remove(productSize);
        productSize.setProductOption(null);
    }

    public boolean equals(AddOrUpdateProductRequest aoupr) {

        return product.getTitle().equals(aoupr.getTitle()) &&
                (product.getBrand()==null ?  aoupr.getBrand().isBlank()  : product.getBrand().getName().equals(aoupr.getBrand())) &&
                (product.getCategory()==null ? aoupr.getCategory()==null : product.getCategory().getId()==aoupr.getCategory()) &&
                underTitle.equals(aoupr.getUnderTitle()) &&
                price.compareTo(aoupr.getPrice())==0 &&
                aoupr.getDiscount()!=null && discount.compareTo(aoupr.getDiscount())==0 &&
                description.equals(aoupr.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, product, productImages, productSizes, underTitle, price, discount, description, addedInDate);
    }
}
