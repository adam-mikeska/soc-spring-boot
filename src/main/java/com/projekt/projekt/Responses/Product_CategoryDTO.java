package com.projekt.projekt.Responses;

import com.projekt.projekt.Models.Ecommerce.Category;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

@Data
public class Product_CategoryDTO {
    private List<Category> categories;
    private Page productOptions;

    public Product_CategoryDTO(List<Category> categories, Page productOptions) {
        this.categories = categories;
        this.productOptions = productOptions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Page getProductOptions() {
        return productOptions;
    }

    public void setProductOptions(Page productOptions) {
        this.productOptions = productOptions;
    }
}
