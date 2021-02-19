package com.projekt.projekt.Controllers;

import com.projekt.projekt.Models.Ecommerce.*;

import com.projekt.projekt.Responses.Product_CategoryDTO;
import com.projekt.projekt.Responses.product_category_autocomplete;
import com.projekt.projekt.Services.ProductService;
import org.apache.commons.io.IOUtils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/latest")
    public List<ProductOption> findLatestProducts() {
        return productService.findLatestProducts();
    }

    @GetMapping("/discounted")
    public List<ProductOption> findDiscountedProducts() {
        return productService.findDiscountedProducts();
    }

    @GetMapping("/popular")
    public List<ProductOption> findMostPopularProducts() {
        return productService.findMostPopularProducts();
    }

    @GetMapping("/autocomplete")
    public product_category_autocomplete autocomplete(@RequestParam(required = true) String search) {
        return productService.autocomplete(search);
    }

    @GetMapping("/carousel")
    public List<CarouselImage> carousel() {
        return productService.getCarousel();
    }

    @GetMapping("/alerts")
    public List<Alert> alerts() {
        return productService.getAllAlerts();
    }

    @GetMapping("/{category}")
    public Product_CategoryDTO findProductsOfCategory(@PathVariable(name = "category", required = true) List<String> categories, @RequestParam(required = false) Integer page, @RequestParam(required = false) BigDecimal priceMin, @RequestParam(required = false) BigDecimal priceMax, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String direction ) {
        Pageable pageable;

        direction=direction == null || !direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc") ? "ASC":direction.toUpperCase();
        sortBy = sortBy == null ? "id" : sortBy;

        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "ASC";
        }

        if (sortBy.equals("price")) {
            Sort sort = (direction.equalsIgnoreCase("desc") ? Sort.by(Sort.Order.desc("price"), Sort.Order.asc("discount")) : Sort.by(Sort.Order.asc("price"), Sort.Order.desc("discount")));
            pageable = PageRequest.of(page==null ? 0 : page, 10, sort);
        } else {
            pageable = PageRequest.of(page==null ? 0 : page, 10, Sort.Direction.valueOf(direction), sortBy);
        }

        return productService.find_products_and_categories(categories, pageable, priceMin, priceMax);
    }

    @GetMapping("/search")
    public Product_CategoryDTO findProductsOfCategory(@RequestParam(required = false) Integer page, @RequestParam(required = false) BigDecimal priceMin, @RequestParam(required = false) BigDecimal priceMax, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String direction , @RequestParam(required = true) String search ) {
        Pageable pageable;

        direction=direction == null || !direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc") ? "ASC":direction.toUpperCase();
        sortBy = sortBy == null ? "id" : sortBy;

        if (sortBy.equals("price")) {
            Sort sort = (direction.equalsIgnoreCase("desc") ? Sort.by(Sort.Order.desc("price"), Sort.Order.asc("discount")) : Sort.by(Sort.Order.asc("price"), Sort.Order.desc("discount")));
            pageable = PageRequest.of(page==null ? 0 : page, 10, sort);
        } else {
            pageable = PageRequest.of(page==null ? 0 : page, 10, Sort.Direction.valueOf(direction),sortBy);
        }

        return productService.find_products_and_categories_by_keyword(search, pageable, priceMin, priceMax);
    }

    @GetMapping("/{product}/{option}")
    public List<ProductOption> allProductOptionsWithSameProduct(@PathVariable String product, @PathVariable String option) {
        return productService.findAllProductOptions(product, option);
    }

    @GetMapping("/categories")
    public List<Category> getUnderCategories(@RequestParam(required = false) String category) {
        if (category == null) {
            category = "";
        }
        return productService.getCategories(category);
    }

    @GetMapping(value = "/image/{product}/{productOption}/{image}")
    public void productImage(HttpServletResponse response, @PathVariable String product, @PathVariable String productOption, @PathVariable String image) {
        Path path = Paths.get("src\\main\\resources\\static\\ProductsImages\\" + product +"\\" + productOption + "\\" + image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        try {
            InputStream is = Files.newInputStream(path);
            IOUtils.copy(is, response.getOutputStream());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while streaming image!");
        }
    }

    @GetMapping(value = "/carousel/image/{image}")
    public void carouselImage(HttpServletResponse response, @PathVariable String image) {
        Path path = Paths.get("src\\main\\resources\\static\\Carousel\\" + image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        try {
            InputStream is = Files.newInputStream(path);
            IOUtils.copy(is, response.getOutputStream());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while streaming image!");
        }
    }

    @GetMapping(value = "/brands/image/{image}")
    public void brandsImage(HttpServletResponse response, @PathVariable String image) {
        Path path = Paths.get("src\\main\\resources\\static\\Brands\\" + image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        try {
            InputStream is = Files.newInputStream(path);
            IOUtils.copy(is, response.getOutputStream());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while streaming image!");
        }
    }

}
