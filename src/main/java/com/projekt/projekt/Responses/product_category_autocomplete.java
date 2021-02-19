package com.projekt.projekt.Responses;

import com.projekt.projekt.Models.Ecommerce.Category;
import com.projekt.projekt.Models.Ecommerce.ProductOption;
import lombok.Data;

import java.util.List;

@Data
public class product_category_autocomplete {
    private List<Category> categories;
    private List<ProductOption> productOptions;

    public product_category_autocomplete() {
    }

    public product_category_autocomplete(List<Category> categories, List<ProductOption> productOptions) {
        this.categories = categories;
        this.productOptions = productOptions;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<ProductOption> getProductOptions() {
        return productOptions;
    }

    public void setProductOptions(List<ProductOption> productOptions) {
        this.productOptions = productOptions;
    }
}
