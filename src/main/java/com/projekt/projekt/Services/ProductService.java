package com.projekt.projekt.Services;

import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Responses.Product_CategoryDTO;
import com.projekt.projekt.Responses.product_category_autocomplete;
import com.projekt.projekt.Repositories.Ecommerce.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class ProductService {

    final private ProductOptionRepository productOptionRepository;
    final private PaymentRepository paymentRepository;
    final private DeliveryRepository deliveryRepository;
    final private CategoryRepository categoryRepository;
    final private CarouselImageRepository carouselRepository;
    final private AlertRepository alertRepository;

    public ProductService(ProductOptionRepository productOptionRepository, PaymentRepository paymentRepository, DeliveryRepository deliveryRepository, CategoryRepository categoryRepository, CarouselImageRepository carouselRepository, AlertRepository alertRepository) {
        this.productOptionRepository = productOptionRepository;
        this.paymentRepository = paymentRepository;
        this.deliveryRepository = deliveryRepository;
        this.categoryRepository = categoryRepository;
        this.carouselRepository = carouselRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * Search products autocomplete
     * @param search - keyword
     * @return
     */

    public product_category_autocomplete autocomplete(String search){
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"));
        return new product_category_autocomplete(categoryRepository.autocompleteCategories(search,pageable).getContent(),productOptionRepository.findAllProductsByKeyWord(search,null,null,pageable).getContent());
    }

    /**
     * Find product option, and its common product options
     * @param productTitle - Product Title
     * @param underTitle - Under Title
     * @return
     */

    public List<ProductOption> findAllProductOptions(String productTitle,String underTitle) {
        List<ProductOption> productOptions = productOptionRepository.findAllByProduct_TitleAndEnabledIsTrue(productTitle);
        ProductOption productOption = productOptionRepository.findByProduct_TitleAndUnderTitleAndEnabledIsTrue(productTitle,underTitle).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found!"));

        if (productOptions.size() > 1) {
            if (productOptions.get(0).getUnderTitle().equals(productOption.getUnderTitle())) {
                return productOptions;
            }
            for (int i = 0; i < productOptions.size(); i++) {
                if (productOptions.get(i).equals(productOption)) {
                    productOptions.set(i, productOptions.get(0));
                    productOptions.set(0, productOption);
                }
            }
        }
        return productOptions;
    }

    public Product_CategoryDTO find_products_and_categories(List<String> categories, Pageable pageable, BigDecimal priceMin, BigDecimal priceMax) {
        Category mainCategory = getCategory(categories);
        return new Product_CategoryDTO(categoryRepository.findAllByParent_Id(mainCategory.getId()),productOptionRepository.findByCategory(mainCategory.getId(),priceMin,priceMax, pageable));
    }

    public Product_CategoryDTO find_products_and_categories_by_keyword(String search, Pageable pageable,BigDecimal priceMin, BigDecimal priceMax){
        return new Product_CategoryDTO(categoryRepository.findAllByNameContaining(search),productOptionRepository.findAllProductsByKeyWord(search,priceMin,priceMax,pageable));
    }

    /**
     * Get Category by its parent categories
     * @param categories - List of parent categories
     * @return
     */

    public Category getCategory(List<String> categories) {
        if (categories == null || categories.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!");
        }

        List<Category> categories1 = new ArrayList<>();
        categories1.add(new Category(decode(categories.get(0)), null));

        for (int i = 1; i < categories.size(); i++) {
            categories1.add(new Category(decode(categories.get(i)), categories1.get(i - 1)));
        }

        List<Category> category = categoryRepository.findAll(Example.of(categories1.get(categories1.size() - 1)));

        if(category== null || category.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found!");
        }

        return category.get(0);
    }

    public String decode(String text){
        try {
            return URLDecoder.decode(text, StandardCharsets.UTF_8.toString());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"");
        }
    }

    public List<Category> getCategories(String category) {
        List<Category> underCategories = categoryRepository.findAllByNameContaining(category);
        return underCategories;
    }

    public List<ProductOption> findDiscountedProducts() {
        return productOptionRepository.findTop4ByOrderByDiscountDesc();
    }

    public List<ProductOption> findLatestProducts() {
        return productOptionRepository.findTop4ByOrderByAddedInDateDesc();
    }
    public List<ProductOption> findMostPopularProducts() {
        return productOptionRepository.findTop4ByOrderBySoldTimes();
    }

    public List<CarouselImage> getCarousel(){
        return carouselRepository.findAll();
    }

    public List<Alert> getAllAlerts(){
        return alertRepository.findAll();
    }
}
