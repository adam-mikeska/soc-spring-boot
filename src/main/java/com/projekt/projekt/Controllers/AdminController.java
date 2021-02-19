package com.projekt.projekt.Controllers;

import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Responses.DashboardDTO;
import com.projekt.projekt.Responses.ProductDto;
import com.projekt.projekt.Models.Role;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Requests.*;
import com.projekt.projekt.Services.AdminService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminController {

    private AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/roles")
    public List<Role> getRoles() {
        return adminService.findAllRoles();
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Integer id) {
        return adminService.getOrder(id);
    }

    @GetMapping("/autocomplete/roles")
    public List<String> getAllRoles() {
        return adminService.getAllRoles();
    }

    @GetMapping("/autocomplete/emails")
    public List<String> getEmails(@RequestParam(required = false) String email) {
        if (email == null) {
            email = "";
        }
        return adminService.getUserEmails(email);
    }

    @GetMapping("/autocomplete/products")
    public List<ProductDto> getProducts(@RequestParam(required = false) String product) {
        if (product == null) {
            product = "";
        }
        return adminService.getProducts(product);
    }

    @GetMapping("/autocomplete/brands")
    public List<String> getBrands(@RequestParam(required = false) String brand) {
        if (brand == null) {
            brand = "";
        }
        return adminService.getBrands(brand);
    }

    @GetMapping("/products")
    public Page<ProductOption> getProducts(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false) String direction, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String search) {

        if (direction == null) {
            direction = "ASC";
        }
        if (sortBy == null) {
            sortBy = "id";
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "ASC";
        }
        if (search == null) {
            search = "";
        }

        Pageable pageable;
        if (sortBy.equals("price")) {
            Sort sort = (direction.equalsIgnoreCase("desc") ? Sort.by(Sort.Order.desc("price"), Sort.Order.asc("discount")) : Sort.by(Sort.Order.asc("price"), Sort.Order.desc("discount")));
            pageable = PageRequest.of(page, size, sort);
        }else if(sortBy.equals("title")){
            pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), "product.title","underTitle");
        } else {
            pageable = PageRequest.of(page, size, Sort.Direction.valueOf(direction.toUpperCase()), sortBy);
        }

        return adminService.findProductOptions(search, pageable);
    }

    @GetMapping("/products/{title}/{underTitle}")
    public ProductOption getProductOption(@PathVariable String title,@PathVariable String underTitle) {
        return adminService.getProductOption(title,underTitle);
    }

    @PostMapping("/products/discount")
    public String discountProducts(@Valid @RequestBody DiscountRequest discountRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.discountProducts(discountRequest);
    }

    @PostMapping("/products")
    public String createProduct(@Valid @ModelAttribute AddOrUpdateProductRequest addOrUpdateProductRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.addProduct(addOrUpdateProductRequest);
    }

    @PutMapping("/products/{id}")
    public String updateProduct(@PathVariable Integer id, @Valid @RequestBody AddOrUpdateProductRequest addOrUpdateProductRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateProduct(id, addOrUpdateProductRequest);
    }

    @PutMapping("/products/{id}/sale")
    public String closeEnableSale(@PathVariable Integer id) {
        return adminService.enableDisableSale(id);
    }

    @PostMapping("/products/{id}/images")
    public ProductOption uploadImageToExistingProduct(@PathVariable Integer id, @RequestPart MultipartFile[] images) {
        return adminService.setImagesToExistingProduct(id, images);
    }

    @DeleteMapping("/products/{productOptionId}/images/{id}")
    public ProductOption deleteImageOfProduct(@PathVariable Integer productOptionId,@PathVariable Integer id) {
        return adminService.deleteImageOfProduct(productOptionId,id);
    }

    @PutMapping("/products/{productOptionId}/images/{id}")
    public ProductOption changeImageOfProduct(@PathVariable Integer productOptionId,@PathVariable Integer id, @RequestPart MultipartFile image) {
        return adminService.editImageOfProductOption(productOptionId,id, image);
    }

    @PostMapping("/products/{productOptionId}/product-sizes")
    public String addProductSize(@PathVariable Integer productOptionId,@Valid @RequestBody ProductSize productSize, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.addProductSize(productOptionId,productSize);
    }

    @PutMapping("/products/{productOptionId}/product-sizes/{id}")
    public String updateProductSize(@PathVariable Integer productOptionId,@PathVariable Integer id,@Valid @RequestBody ProductSize productSize, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateProductSize(productOptionId,id,productSize);
    }

    @DeleteMapping("/products/{productOptionId}/product-sizes/{id}")
    public String deleteProductSize(@PathVariable Integer productOptionId,@PathVariable Integer id) {
        return adminService.deleteProductSize(productOptionId,id);
    }

    @GetMapping("/orders")
    public Page<Order> getOrders(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false) String direction, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String search) {

        if (direction == null) {
            direction = "ASC";
        }
        if (sortBy == null) {
            sortBy = "id";
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "ASC";
        }
        if (search == null) {
            search = "";
        }

        return adminService.findAllOrders(search, PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(direction.toUpperCase()), sortBy)));
    }

    @PutMapping("/orders/{id}")
    public String updateOrder(@Valid @RequestBody Order editOrderRequest, BindingResult result, @PathVariable Integer id) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateOrder(id, editOrderRequest);
    }

    @PostMapping("/orders/{orderId}/order-items")
    public String addOrderItem(@PathVariable Integer orderId,@Valid @RequestBody AddToCartRequest addOIrequest,BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }

        return adminService.addOrderItem(orderId,addOIrequest);
    }

    @PutMapping("/orders/{orderId}/order-items/{id}")
    public String updateOrderItem(@PathVariable Integer orderId,@PathVariable Integer id,@RequestParam Integer quantity) {
        return adminService.updateOrderItem(orderId,id,quantity);
    }

    @DeleteMapping("/orders/{orderId}/order-items/{id}")
    public String deleteOrderItem(@PathVariable Integer orderId,@PathVariable Integer id) {
        return adminService.deleteOrderItem(orderId,id);
    }

    @PutMapping("/orders/{orderId}/coupon")
    public String setCouponToOrder(@PathVariable Integer orderId,@RequestParam String coupon) {
        return adminService.setCoupon(orderId,coupon);
    }

    @GetMapping("/users")
    public Page<User> getUsers(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false) String direction, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String search) {
        if (direction == null) {
            direction = "ASC";
        }
        if (sortBy == null) {
            sortBy = "id";
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "ASC";
        }
        if (search == null) {
            search = "";
        }
        return adminService.findAllUsers(search, PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(direction.toUpperCase()), sortBy)));
    }

    @PutMapping("/users/{id}")
    public String updateUser(@Valid @RequestBody User requestForm, BindingResult result, @PathVariable Integer id) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateUser(id, requestForm);
    }

    @PutMapping("/users/{id}/image")
    public String changeImageOfUser(@PathVariable Integer id){
        return adminService.changeImageOfUser(id);
    }

    @GetMapping("/users/{id}/orders")
    public Page<Order> getUsersOrders(@PathVariable Integer id,@RequestParam Integer page, @RequestParam Integer size) {
        return adminService.findUsersOrders(id,  PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
    }

    @PostMapping("/users/asign-role")
    public String asignRole(@Valid @RequestBody AsignRoleRequest request, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.asignRole(request);
    }

    @PostMapping("/users/lock")
    public String lockUser(@Valid @RequestBody LockUserRequest request, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, result.getFieldError().getDefaultMessage());
        }
        return adminService.lockUser(request);
    }

    @PostMapping("/send-email")
    public String sendEmail(@ModelAttribute SendEmailRequest request) {
        return adminService.sendEmail(request);
    }

    @GetMapping("/coupons")
    public Page<Coupon> getCoupons(@RequestParam Integer page, @RequestParam Integer size, @RequestParam(required = false) String direction, @RequestParam(required = false) String sortBy, @RequestParam(required = false) String search) {
        if (direction == null) {
            direction = "ASC";
        }
        if (sortBy == null) {
            sortBy = "id";
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            direction = "ASC";
        }
        if (search == null) {
            search = "";
        }

        return adminService.findAllCoupons(search, PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(direction.toUpperCase()), sortBy)));
    }

    @PostMapping("/coupons")
    public String createCoupon(@Valid @RequestBody Coupon coupon,BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.createCoupon(coupon);
    }

    @PutMapping("/coupons/{id}")
    public String enableDisableCoupon(@PathVariable Integer id) {
        return adminService.enableDisableCoupon(id);
    }

    @PostMapping("/roles")
    public String createRole(@Valid @RequestBody Role requestForm, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.createRole(requestForm);
    }

    @PutMapping("/roles/{id}")
    public String updateRole(@PathVariable Integer id, @Valid @RequestBody Role requestForm, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateRole(id, requestForm);
    }

    @DeleteMapping("/roles/{id}")
    public String deleteRole(@PathVariable Integer id) {
        return adminService.deleteRole(id);
    }

    @GetMapping("/brands")
    public List<Brand> findAllBrands() {
        return adminService.findAllBrands();
    }

    @PostMapping("/brands")
    public String addBrand(@Valid @ModelAttribute AddBrandRequest addBrandRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.addBrand(addBrandRequest);
    }

    @PutMapping("/brands/{id}")
    public String updateBrand(@PathVariable Integer id, @RequestParam String brand) {
        return adminService.updateBrand(id, brand);
    }

    @DeleteMapping("/brands/{id}")
    public String deleteBrand(@PathVariable Integer id) {
        return adminService.deleteBrand(id);
    }

    @PutMapping("/brands/{id}/images")
    public String changeImageOfBrand(@PathVariable Integer id, @RequestPart MultipartFile image) {
        return adminService.changeImageOfBrand(id, image);
    }

    @PostMapping("/categories")
    public String addCategory(@Valid @RequestBody ManageCategory manageCategory, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.addCategory(manageCategory);
    }

    @PutMapping("/categories/{id}")
    public String updateCategory(@PathVariable Integer id, @Valid @RequestBody ManageCategory manageCategory, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return adminService.updateCategory(id,manageCategory);
    }

    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable Integer id) {
        return adminService.deleteCategory(id);
    }

    @PostMapping("/alerts")
    public String addNewAlert(@Valid @RequestBody Alert alert,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, bindingResult.getFieldError().getDefaultMessage());
        }
        return adminService.addAlert(alert);
    }

    @PutMapping("/alerts/{id}")
    public String updateAlert(@PathVariable Integer id,@Valid @RequestBody Alert alert,BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, bindingResult.getFieldError().getDefaultMessage());
        }
        return adminService.updateAlert(id,alert);
    }

    @DeleteMapping("/alerts/{id}")
    public String deleteAlert(@PathVariable Integer id) {
        return adminService.deleteAlert(id);
    }

    @PostMapping("/carousel")
    public String addCarousel(@Valid @ModelAttribute AddCarouselRequest carousel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, bindingResult.getFieldError().getDefaultMessage());
        }
        return adminService.addNewCarouselImage(carousel);
    }

    @PutMapping("/carousel/{id}")
    public String updateCarousel(@PathVariable Integer id, @Valid @RequestBody CarouselImage carousel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, bindingResult.getFieldError().getDefaultMessage());
        }
        return adminService.updateCarouselImage(id,carousel);
    }

    @DeleteMapping("/carousel/{id}")
    public String deleteCarousel(@PathVariable Integer id) {
        return adminService.deleteCarouselImage(id);
    }

    @PutMapping("/payments/{id}")
    public String changePriceOfPayment(@PathVariable Integer id, @RequestParam BigDecimal price) {
        return adminService.changePriceOfpayment(id,price);
    }

    @GetMapping("/dashboard")
    public DashboardDTO dashboardDTO() {
        return adminService.dashboardData();
    }

    @PutMapping("/deliveries/{id}")
    public String changePriceOfDelivery(@PathVariable Integer id,@RequestParam BigDecimal price) {
        return adminService.changePriceofDelivery(id,price);
    }
}
