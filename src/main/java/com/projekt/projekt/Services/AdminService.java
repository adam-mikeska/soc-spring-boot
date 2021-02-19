package com.projekt.projekt.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Enums.GENDER;
import com.projekt.projekt.Enums.OrderState;
import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Responses.DashboardDTO;
import com.projekt.projekt.Responses.ProductDto;
import com.projekt.projekt.Models.Role;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Repositories.Ecommerce.*;
import com.projekt.projekt.Repositories.RoleRepository;
import com.projekt.projekt.Repositories.UserRepository;
import com.projekt.projekt.Requests.*;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class AdminService {

    final private UserRepository userRepository;
    final private RoleRepository roleRepository;
    final private OrderRepository orderRepository;
    final private OrderItemRepository orderItemRepository;
    final private ProductRepository productRepository;
    final private BrandRepository brandRepository;
    final private ProductSizeRepository productSizeRepository;
    final private ProductOptionRepository productOptionRepository;
    final private ProductImageRepository productImageRepository;
    final private CartItemRepository cartItemRepository;
    final private CartRepository cartRepository;
    final private CouponRepository couponRepository;
    final private CategoryRepository categoryRepository;
    final private MailService mailService;
    final private CarouselImageRepository carouselImageRepository;
    final private AlertRepository alertRepository;
    final private DeliveryRepository deliveryRepository;
    final private PaymentRepository paymentRepository;
    @Value("${frontend}")
    private String FRONT_END;

    public AdminService(UserRepository userRepository, RoleRepository roleRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, BrandRepository brandRepository, ProductSizeRepository productSizeRepository, ProductOptionRepository productOptionRepository, ProductImageRepository productImageRepository, CartItemRepository cartItemRepository, CartRepository cartRepository, CouponRepository couponRepository, CategoryRepository categoryRepository, MailService mailService, CarouselImageRepository carouselImageRepository, AlertRepository alertRepository, DeliveryRepository deliveryRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.productSizeRepository = productSizeRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.couponRepository = couponRepository;
        this.categoryRepository = categoryRepository;
        this.mailService = mailService;
        this.carouselImageRepository = carouselImageRepository;
        this.alertRepository = alertRepository;
        this.deliveryRepository = deliveryRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Create role
     *
     * @param requestForm - JSON body
     * @return
     */

    public String createRole(Role requestForm) {
        if (requestForm.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please enter name!");
        }
        if (roleRepository.existsByName(requestForm.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Role with this name already exists!");
        }

        roleRepository.save(new Role(requestForm.getName(), requestForm.getPermissions(), requestForm.getColor()));
        return "Sucessfully created role!";
    }

    /**
     * Update role
     *
     * @param id          - Role Id
     * @param requestForm - JSON body
     * @return
     */

    public String updateRole(Integer id, Role requestForm) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        if (role.equals(requestForm)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }
        if (roleRepository.existsByName(requestForm.getName()) && !role.getName().equals(requestForm.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role with this name already exists!");
        }
        if (role.getName().equals("USER") || role.getName().equals("OWNER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Default roles cannot be updated!");
        }

        role.setColor(requestForm.getColor());
        role.setName(requestForm.getName());
        role.setPermissions(requestForm.getPermissions());
        roleRepository.save(role);

        return "Sucessfully updated role!";
    }

    /**
     * Delete role
     *
     * @param id - Role Id
     * @return
     */

    public String deleteRole(Integer id) {
        Role role = roleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        if (role.getName().equals("USER") || role.getName().equals("OWNER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Default role cannot be deleted.");
        }

        List<User> users = userRepository.findAllByRole(role);
        users.stream().forEach(u -> u.setRole(userRole));

        userRepository.saveAll(users);
        roleRepository.delete(role);
        return "Sucessfully deleted role!";
    }

    /**
     * Asign role to user
     *
     * @param request - JSON body
     * @return
     */

    public String asignRole(AsignRoleRequest request) {
        if (request.getUsers() == null || request.getUsers().length == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't provided any users!");
        }
        Role role = roleRepository.findByName(request.getRoleName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));

        Integer nonExistingUsers = 0;

        for (String email : request.getUsers()) {
            if (userRepository.existsByEmail(email)) {
                User user = userRepository.findByEmail(email);
                user.setRole(role);
                userRepository.save(user);
            } else {
                nonExistingUsers++;
            }
        }

        if (nonExistingUsers == request.getUsers().length) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users do not exist!");
        }

        return "Sucessfully asigned role!" + (nonExistingUsers > 0 ? " But some users were not found!" : "");
    }

    /**
     * Lock user
     *
     * @param request - JSON body
     * @return
     */

    public String lockUser(LockUserRequest request) {
        if (request.getUsers() == null || request.getUsers().length == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't provided any users!");
        }
        Integer nonExistingUsers = 0;

        for (String user : request.getUsers()) {
            if (userRepository.existsByEmail(user)) {
                User userr = userRepository.findByEmail(user);
                setLockedTill(request, userr);
            } else {
                nonExistingUsers++;
            }
        }

        if (nonExistingUsers == request.getUsers().length) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users do not exist!");
        }

        if (!request.getTimeUnit().equals("PERMANENTLY")) {
            unlockAfterTimeIsExpired(request);
        }
        return "Sucessfully blocked!" + (nonExistingUsers > 0 ? " But some users were not found!" : "");
    }

    public void setLockedTill(LockUserRequest request, User userr) {
        LocalDateTime dateStart = LocalDateTime.now();

        switch (request.getTimeUnit()) {
            case "MINUTES":
                dateStart = dateStart.plusMinutes(request.getLockTime());
                userr.setLockedTill(dateStart.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")));
                break;
            case "HOURS":
                dateStart = dateStart.plusHours(request.getLockTime());
                userr.setLockedTill(dateStart.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")));
                break;
            case "DAYS":
                dateStart = dateStart.plusDays(request.getLockTime());
                userr.setLockedTill(dateStart.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss")));
                break;
            default:
                userr.setLockedTill("PERMANENTLY");
        }

        userr.setNonLocked(false);
        userRepository.save(userr);
    }

    public void unlockAfterTimeIsExpired(LockUserRequest request) {
        String date = LocalDateTime.now().plusMinutes(request.getLockTime()).format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss"));

        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (String user : request.getUsers()) {
                    if (userRepository.existsByEmail(user)) {
                        User userr = userRepository.findByEmail(user);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

                        if (!LocalDateTime.parse(date, formatter).isBefore(LocalDateTime.parse(userr.getLockedTill(), formatter))) {
                            userr.setLockedTill(null);
                            userr.setNonLocked(true);
                            userRepository.save(userr);
                        }
                    }
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, request.getLockTime(), TimeUnit.valueOf(request.getTimeUnit()));
    }

    /**
     * Update user
     *
     * @param id          - User Id
     * @param requestForm - JSON body
     * @return
     */

    public String updateUser(Integer id, User requestForm) {
        User memberFromDb = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        if (memberFromDb.equals(requestForm)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }
        if (userRepository.existsByEmail(requestForm.getEmail()) && !memberFromDb.getEmail().equals(requestForm.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists!");
        }
        if (requestForm.getTelNumber() != null && userRepository.existsByTelNumber(requestForm.getTelNumber()) && !requestForm.getTelNumber().equals(memberFromDb.getTelNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This mobile number is already assigned to one of our users.");
        }
        memberFromDb.setValues(requestForm, true);

        userRepository.save(memberFromDb);
        return "Sucessfully updated!";
    }

    /**
     * Change image of user
     *
     * @param id - User Id
     * @return
     */

    public String changeImageOfUser(Integer id) {
        User memberFromDb = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
        String img = memberFromDb.getGender().equals(GENDER.Male) ? "img_avatar_male.png" : "img_avatar_female.png";

        if (img.equals(memberFromDb.getImage())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not update image!");
        }
        new File("src\\main\\resources\\static\\UsersImages\\" + memberFromDb.getImage()).delete();

        memberFromDb.setImage(img);
        userRepository.save(memberFromDb);
        return "Successfully changed image";
    }

    /**
     * Send email
     *
     * @param request - JSON body
     * @return
     */

    public String sendEmail(SendEmailRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("text", request.getContent());
        mailService.sendEmail(request, model, "email.ftl");
        return "Successfully sent email!";
    }

    /**
     * Update order
     *
     * @param id               - Order Id
     * @param editOrderRequest - JSON body
     * @return
     */

    public String updateOrder(Integer id, Order editOrderRequest) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found!"));

        if (order.equals(editOrderRequest)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything");
        }

        order.setName(editOrderRequest.getName());
        order.setTelNumber(editOrderRequest.getTelNumber());
        order.setEmail(editOrderRequest.getEmail());
        order.setCountry(editOrderRequest.getCountry());
        order.setCity(editOrderRequest.getCity());
        order.setAddress(editOrderRequest.getAddress());
        order.setPostalCode(editOrderRequest.getPostalCode());

        if (order.getOrderState() != editOrderRequest.getOrderState()) {
            sendEmailOrderStateChange(order, editOrderRequest);
        }
        order.setOrderState(editOrderRequest.getOrderState());

        orderRepository.save(order);
        return "Successfully updated order!";
    }

    /**
     * Add Order Item to existing order
     *
     * @param orderId      - order id
     * @param addOIrequest - JSON body
     * @return
     */

    public String addOrderItem(Integer orderId, AddToCartRequest addOIrequest) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        ProductOption productOption = productOptionRepository.findByProduct_TitleAndUnderTitle(addOIrequest.getProduct(), addOIrequest.getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        Boolean orderItemExists = orderItemRepository.existsByOrder_IdAndProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(order.getId(), addOIrequest.getProduct(), addOIrequest.getUnderTitle(), addOIrequest.getSize());
        OrderItem orderItem = (orderItemExists ? orderItemRepository.findByOrder_IdAndProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(order.getId(), addOIrequest.getProduct(), addOIrequest.getUnderTitle(), addOIrequest.getSize()) : new OrderItem(addOIrequest.getQuantity(), (productOption.getPrice().subtract(productOption.getDiscount())).multiply(BigDecimal.valueOf(addOIrequest.getQuantity())), productOption, addOIrequest.getSize(), order));
        ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(addOIrequest.getSize(), addOIrequest.getProduct(), addOIrequest.getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size was not found!"));

        if (productSize.getOnStock() - addOIrequest.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough of product on stock! On stock: " + productSize.getOnStock());
        }

        productSize.setOnStock(productSize.getOnStock() - addOIrequest.getQuantity());
        productSizeRepository.save(productSize);

        proceedAddOrderItem(orderItem, order, orderItemExists, addOIrequest);

        return "Successfully " + (orderItemExists ? "updated" : "added") + " order item!";
    }

    public void proceedAddOrderItem(OrderItem orderItem, Order order, Boolean orderItemExists, AddToCartRequest addOIrequest) {
        if (orderItemExists) {
            order.setTotalPrice(order.getTotalPrice().add(orderItem.getPrice().divide(BigDecimal.valueOf(orderItem.getQuantity())).multiply(BigDecimal.valueOf(addOIrequest.getQuantity()))));
            orderItem.setPrice(orderItem.getPrice().add(orderItem.getPrice().divide(BigDecimal.valueOf(orderItem.getQuantity())).multiply(BigDecimal.valueOf(addOIrequest.getQuantity()))));
            orderItem.setQuantity(orderItem.getQuantity() + addOIrequest.getQuantity());
        } else {
            order.addOrderItem(orderItem);
            order.setTotalPrice(order.getTotalPrice().add(orderItem.getPrice()));
        }
        orderItemRepository.save(orderItem);
        orderRepository.save(order);
    }

    /**
     * Update Order Item
     *
     * @param orderId  - Order Id
     * @param id       - Order Item Id
     * @param quantity - New Quantity
     * @return
     */

    public String updateOrderItem(Integer orderId, Integer id, Integer quantity) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(id, orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found!"));

        ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(orderItem.getProductSize(), orderItem.getProductOption().getProduct().getTitle(), orderItem.getProductOption().getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size not found!"));

        if (productSize.getOnStock() + orderItem.getQuantity() - quantity < 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough of product on stock! On stock: " +productSize.getOnStock());
        }
        if (orderItem.getQuantity() == quantity) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated order item!");
        }

        productSize.setOnStock(productSize.getOnStock() + orderItem.getQuantity() - quantity);
        productSizeRepository.save(productSize);

        proceedUpdateOrderItem(order, orderItem, quantity);

        return "Successfully " + (quantity < 1 ? "deleted" : "updated") + " order item!";
    }

    public void proceedUpdateOrderItem(Order order, OrderItem orderItem, Integer quantity) {
        BigDecimal pricePerOne = orderItem.getPrice().divide(BigDecimal.valueOf(orderItem.getQuantity()));

        order.setTotalPrice((order.getTotalPrice().subtract(orderItem.getPrice())).add((pricePerOne).multiply(BigDecimal.valueOf(quantity))));

        orderItem.getProductOption().setSoldTimes((orderItem.getProductOption().getSoldTimes() - orderItem.getQuantity()) + quantity);
        orderItem.setQuantity(quantity);
        orderItem.setPrice((pricePerOne).multiply(BigDecimal.valueOf(quantity)));

        if (quantity < 1) {
            if (order.getOrderItems().size() == 1) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not delete last order item, you can cancel the order!");
            }
            order.removeOrderItem(orderItem);

            orderItemRepository.delete(orderItem);
        } else {
            orderItemRepository.save(orderItem);
        }

        productOptionRepository.save(orderItem.getProductOption());
        orderRepository.save(order);
    }

    /**
     * Delete Order Item
     *
     * @param orderId - Order Id
     * @param id      - Order Item Id
     * @return
     */

    public String deleteOrderItem(Integer orderId, Integer id) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        OrderItem orderItem = orderItemRepository.findByIdAndOrder_Id(id, orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found!"));

        if (order.getOrderItems().size() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not delete last order item, you can cancel the order!");
        }

        ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(orderItem.getProductSize(), orderItem.getProductOption().getProduct().getTitle(), orderItem.getProductOption().getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size not found!"));
        productSize.setOnStock(productSize.getOnStock() + orderItem.getQuantity());
        productSizeRepository.save(productSize);

        proceedDeleteOrderItem(order, orderItem);

        return "Successfully deleted order item!";
    }

    public void proceedDeleteOrderItem(Order order, OrderItem orderItem) {
        orderItem.getProductOption().setSoldTimes(orderItem.getProductOption().getSoldTimes() - orderItem.getQuantity());

        order.setTotalPrice(order.getTotalPrice().subtract(orderItem.getPrice()));
        order.removeOrderItem(orderItem);

        productOptionRepository.save(orderItem.getProductOption());
        orderItemRepository.delete(orderItem);
        orderRepository.save(order);
    }

    /**
     * Set coupon to order
     * @param orderId - order id
     * @return
     */

    public String setCoupon(Integer orderId,String code) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        return "Successfully set coupon!";
    }


    /**
     * Send email after order state changes
     *
     * @param order            - Existing order
     * @param editOrderRequest - JSON body
     */

    public void sendEmailOrderStateChange(Order order, Order editOrderRequest) {
        Map<String, Object> model = new HashMap<>();
        model.put("order", order);
        model.put("link", FRONT_END + "/checkout/" + order.getId());
        String text;

        switch (editOrderRequest.getOrderState()) {
            case PROCESSING:
                text = "Order state was changed to processing!<br> We will work as hard as we can, to deliver the order to you, as soon as possible!";
                break;
            case PAID:
                text = "Order state was changed to paid!<br>We will work as hard as we can, to deliver the order to you, as soon as possible!";
                break;
            case AWAITING_PAYMENT:
                text = "Order state was changed to Awaiting Payment!<br>Please make payment as fast as you can!";
                break;
            case SHIPPED:
                text = "Order state was changed to shipped!<br>We have just shipped your order!";
                break;
            case COMPLETED:
                text = "Order state was changed to completed!<br><br>Thanks for your order!<br>We hope to see you again soon!";
                break;
            case REFUNDED:
                text = "Order state was changed to refunded!<br><br>We are sorry to hear that!<br>We hope to see you again soon!";
                break;
            case CANCELED:
                text = "Order state was changed to canceled!<br><br>We are sorry to hear that!<br>We hope to see you again soon!";
                break;
            default:
                text = "Thanks for your order!";
                break;
        }

        model.put("text", text);
        mailService.sendEmail(new SendEmailRequest(new String[]{order.getEmail()}, "Order id: " + order.getId() + " " + editOrderRequest.getOrderState().toString().toLowerCase().replace("_", ""), null), model, "invoice.ftl");
    }

    /**
     * Add new product
     *
     * @param addOrUpdateProductRequest - JSON body
     * @return
     */

    public String addProduct(AddOrUpdateProductRequest addOrUpdateProductRequest) {
        if (addOrUpdateProductRequest.getDiscount() != null && addOrUpdateProductRequest.getDiscount().compareTo(addOrUpdateProductRequest.getPrice()) == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount can not be higher than price!");
        }
        if (productOptionRepository.existsByProduct_TitleAndUnderTitle(addOrUpdateProductRequest.getTitle(), addOrUpdateProductRequest.getUnderTitle())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists!");
        }
        if (addOrUpdateProductRequest.getImages() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please select atleast 1 image!");
        }

        proceedAddProduct((productRepository.existsByTitle(addOrUpdateProductRequest.getTitle()) ? productRepository.findByTitle(addOrUpdateProductRequest.getTitle()) : new Product()), addOrUpdateProductRequest, new ProductOption());

        return "Successfully created product!";
    }

    public void proceedAddProduct(Product product, AddOrUpdateProductRequest addOrUpdateProductRequest, ProductOption productOption) {
        List<ProductSize> productSizes = getSizes(addOrUpdateProductRequest.getProductSizes(), productOption);
        List<ProductImage> productImages = getProductImages(addOrUpdateProductRequest, productOption);

        product.setTitle(addOrUpdateProductRequest.getTitle());
        product.setBrand((addOrUpdateProductRequest.getBrand() == null || !brandRepository.existsByName(addOrUpdateProductRequest.getBrand()) ? null : brandRepository.findByName(addOrUpdateProductRequest.getBrand())));
        product.setCategory((addOrUpdateProductRequest.getCategory() == null ? null : categoryRepository.findById(addOrUpdateProductRequest.getCategory()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found!"))));

        productOption.setPrice(addOrUpdateProductRequest.getPrice());
        productOption.setDiscount((addOrUpdateProductRequest.getDiscount() == null ? new BigDecimal(0) : addOrUpdateProductRequest.getDiscount()));
        productOption.setUnderTitle(addOrUpdateProductRequest.getUnderTitle());
        productOption.setProduct(product);
        productOption.setDescription(addOrUpdateProductRequest.getDescription());
        productOption.setEnabled(addOrUpdateProductRequest.getEnabled());

        productRepository.save(product);
        productOptionRepository.save(productOption);
        productSizeRepository.saveAll(productSizes);
        productImageRepository.saveAll(productImages);

        productOption.setProductSizes(productSizes);
        productOption.setProductImages(productImages);

        productOptionRepository.save(productOption);
    }

    public List<ProductSize> getSizes(String sizes, ProductOption productOption) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        Set<String> nameSet = new HashSet<>();
        List<ProductSize> productSizes;

        try {
            productSizes = objectMapper.readValue("[" + sizes + "]", new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving product sizes!");
        }

        productSizes = productSizes.stream()
                .filter(e -> nameSet.add(e.getSize().trim()))
                .peek(e -> e.setProductOption(productOption))
                .collect(Collectors.toList());

        return productSizes;
    }

    public List<ProductImage> getProductImages(AddOrUpdateProductRequest addOrUpdateProductRequest, ProductOption productOption) {
        List<ProductImage> productImages = new ArrayList<>();
        List<String> contentTypes = Arrays.asList("image/png", "image/jpeg", "image/gif");

        new File("src\\main\\resources\\static\\ProductsImages\\" + addOrUpdateProductRequest.getTitle() + "\\" + addOrUpdateProductRequest.getUnderTitle()).mkdirs();

        for (int i = 0; i < addOrUpdateProductRequest.getImages().length; i++) {

            if (!contentTypes.contains(addOrUpdateProductRequest.getImages()[i].getContentType())) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Bad content type!");
            }

            String[] parts = addOrUpdateProductRequest.getImages()[i].getOriginalFilename().split("\\.");
            String imageName = UUID.randomUUID() + "." + parts[1];

            try {
                Files.copy(addOrUpdateProductRequest.getImages()[i].getInputStream(), Paths.get("src\\main\\resources\\static\\ProductsImages\\" + addOrUpdateProductRequest.getTitle() + "\\" + addOrUpdateProductRequest.getUnderTitle() + "\\" + imageName), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while saving images!");
            }

            productImages.add(new ProductImage(imageName, productOption));
        }

        return productImages;
    }

    /**
     * Update product
     *
     * @param id                        - Product Id
     * @param addOrUpdateProductRequest - JSON body
     * @return
     */

    public String updateProduct(Integer id, AddOrUpdateProductRequest addOrUpdateProductRequest) {
        ProductOption productOption = productOptionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product option not found!"));

        if (addOrUpdateProductRequest.getDiscount() != null && addOrUpdateProductRequest.getDiscount().compareTo(addOrUpdateProductRequest.getPrice()) == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Discount can not be bigger than price!");
        }
        if (productOptionRepository.existsByProduct_TitleAndUnderTitle(addOrUpdateProductRequest.getUnderTitle(), addOrUpdateProductRequest.getTitle()) && productOption.getId() != id) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product already exists!");
        }
        if (productOption.equals(addOrUpdateProductRequest)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }

        proceedUpdateProduct(!productRepository.existsByTitle(addOrUpdateProductRequest.getTitle()), productOption, addOrUpdateProductRequest);

        return "Successfully updated!";
    }

    public void proceedUpdateProduct(Boolean isNew, ProductOption productOption, AddOrUpdateProductRequest addOrUpdateProductRequest) {
        String tempProductTitle = productOption.getProduct().getTitle();
        copyFolder(productOption, addOrUpdateProductRequest);

        Product product = (isNew ? new Product() : productRepository.findByTitle(addOrUpdateProductRequest.getTitle()));
        product.setTitle(addOrUpdateProductRequest.getTitle());
        product.setBrand((addOrUpdateProductRequest.getBrand() == null || !brandRepository.existsByName(addOrUpdateProductRequest.getBrand()) ? null : brandRepository.findByName(addOrUpdateProductRequest.getBrand())));
        product.setCategory((addOrUpdateProductRequest.getCategory() == null ? null : categoryRepository.findById(addOrUpdateProductRequest.getCategory()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found!"))));

        productOption.setPrice(addOrUpdateProductRequest.getPrice());
        productOption.setDiscount((addOrUpdateProductRequest.getDiscount() == null ? new BigDecimal(0) : addOrUpdateProductRequest.getDiscount()));
        productOption.setUnderTitle(addOrUpdateProductRequest.getUnderTitle());
        productOption.setProduct(product);
        productOption.setDescription(addOrUpdateProductRequest.getDescription());

        productRepository.save(product);
        productOptionRepository.save(productOption);

        if (!productOptionRepository.existsByProduct_Title(tempProductTitle)) {
            productRepository.delete(productRepository.findByTitle(tempProductTitle));
        }
    }

    public void copyFolder(ProductOption productOption, AddOrUpdateProductRequest addOrUpdateProductRequest) {
        Path FROM = Paths.get("src\\main\\resources\\static\\ProductsImages\\" + productOption.getProduct().getTitle() + "\\" + productOption.getUnderTitle());
        Path TO = Paths.get("src\\main\\resources\\static\\ProductsImages\\" + addOrUpdateProductRequest.getTitle() + "\\" + addOrUpdateProductRequest.getUnderTitle());

        try {
            if (Files.exists(TO) && Files.isSameFile(FROM, TO)) {
                return;
            }
            FileUtils.copyDirectory(FROM.toFile(), TO.toFile());
            FileUtils.deleteDirectory(FROM.toFile());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while copying folder!");
        }

        if (FROM.getParent().toFile().listFiles().length < 1) {
            FROM.getParent().toFile().delete();
        }
    }

    /**
     * Upload images to existing product
     *
     * @param id     - ProductOption Id
     * @param images - Array of images
     * @return
     */

    public ProductOption setImagesToExistingProduct(Integer id, MultipartFile[] images) {
        List<String> contentTypes = Arrays.asList("image/png", "image/jpeg", "image/gif");
        ProductOption productOption = productOptionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        for (int i = 0; i < images.length; i++) {

            if (!contentTypes.contains(images[i].getContentType())) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Bad content type!");
            }

            String[] parts = images[i].getOriginalFilename().split("\\.");
            String imageName = UUID.randomUUID() + "." + parts[1];

            try {
                Files.copy(images[i].getInputStream(), Paths.get("src\\main\\resources\\static\\ProductsImages\\" + productOption.getProduct().getTitle() + "\\" + productOption.getUnderTitle() + "\\" + imageName));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while setting image!");
            }

            productOption.addProductImage(new ProductImage(imageName, productOption));
        }

        productImageRepository.saveAll(productOption.getProductImages());
        productOptionRepository.save(productOption);

        return productOption;
    }

    /**
     * Delete image of product
     *
     * @param productOptionId - Product Option ID
     * @param id              - Product Image Id
     * @return
     */

    public ProductOption deleteImageOfProduct(Integer productOptionId, Integer id) {
        ProductOption productOption = productOptionRepository.findById(productOptionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        ProductImage productImage = productImageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product image not found!"));

        if (productOption.getProductImages().size() <= 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "There has to be atleast 1 images!");
        }

        productOption.removeProductImage(productImage);

        new File("src\\main\\resources\\static\\ProductsImages\\" + productOption.getProduct().getTitle() + "\\" + productOption.getUnderTitle() + "\\" + productImage.getImage()).delete();

        productImageRepository.delete(productImage);
        productOptionRepository.save(productOption);

        return productOption;
    }

    /**
     * Edit image of product option
     *
     * @param productOptionId - Product Option Id
     * @param id              - Product Image Id
     * @param file            - new Image
     * @return
     */

    public ProductOption editImageOfProductOption(Integer productOptionId, Integer id, MultipartFile file) {
        ProductOption productOption = productOptionRepository.findById(productOptionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        ProductImage productImage = productImageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product image found!"));
        List<String> contentTypes = Arrays.asList("image/png", "image/jpeg", "image/gif");

        if (!contentTypes.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Please upload only images!");
        }

        try {
            Files.copy(file.getInputStream(), Paths.get("src\\main\\resources\\static\\ProductsImages\\" + productImage.getProductOption().getProduct().getTitle() + "\\" + productImage.getProductOption().getUnderTitle() + "\\" + productImage.getImage()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while edititing image!");
        }

        return productImage.getProductOption();
    }

    /**
     * Close or enable sale of product option
     *
     * @param id - Product Option Id
     * @return
     */

    public String enableDisableSale(Integer id) {
        ProductOption productOption = productOptionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        productOption.setEnabled(!productOption.getEnabled());

        if (!productOption.getEnabled()) {
            List<CartItem> cartItems = cartItemRepository.findAllByProductOption_UnderTitle(productOption.getUnderTitle());
            cartItems.stream().forEach(s -> s.getCart().removeCartItem(s));
            cartItemRepository.deleteAll(cartItems);
        }

        productOptionRepository.save(productOption);
        return "Successfully " + (productOption.getEnabled() ? "enabled sale!" : "closed sale!");
    }

    /**
     * Add Product Size
     *
     * @param productOptionId - Product Option Id
     * @param request         - JSON body
     * @return
     */

    public String addProductSize(Integer productOptionId, ProductSize request) {
        ProductOption productOption = productOptionRepository.findById(productOptionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product option not found!"));

        if (productSizeRepository.existsBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(request.getSize(), productOption.getProduct().getTitle(), productOption.getUnderTitle())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already exists");
        }

        productSizeRepository.save(request);
        productOption.addProductSize(request);
        productOptionRepository.save(productOption);

        return "Successfully added size!";
    }

    /**
     * Update Product Size
     *
     * @param productOptionId - Product Option Id
     * @param id              - Product Size Id
     * @param request         - JSON body
     * @return
     */

    public String updateProductSize(Integer productOptionId, Integer id, ProductSize request) {
        ProductOption productOption = productOptionRepository.findById(productOptionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        ProductSize productSize = productSizeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        if (productSizeRepository.existsBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(request.getSize(), productSize.getProductOption().getProduct().getTitle(), productSize.getProductOption().getUnderTitle()) && !productSize.getSize().equals(request.getSize())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already exists");
        }
        if (productSize.getSize().equals(request.getSize()) && productSize.getOnStock().equals(request.getOnStock())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }

        if (!productSize.getSize().equals(request.getSize())) {
            List<CartItem> cartItems = cartItemRepository.findAllByProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(productOption.getProduct().getTitle(), productOption.getUnderTitle(), productSize.getSize());
            cartItems.stream().forEach(s -> s.setProductSize(request.getSize()));
            cartItemRepository.saveAll(cartItems);
        }

        productSize.setSize(request.getSize());
        productSize.setOnStock(request.getOnStock());
        productSizeRepository.save(productSize);

        return "Successfully updated product size!";
    }

    /**
     * Delete Product Size
     *
     * @param productOptionId - Product Option Id
     * @param id              - Product Size Id
     * @return
     */

    public String deleteProductSize(Integer productOptionId, Integer id) {
        ProductOption productOption = productOptionRepository.findById(productOptionId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        ProductSize productSize = productSizeRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        if (productOption.getProductSizes().size() < 2) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete product size, there has to be atleast 1!");
        }

        List<CartItem> cartItems = cartItemRepository.findAllByProductOption_Product_TitleAndProductOption_UnderTitleAndProductSize(productOption.getProduct().getTitle(), productOption.getUnderTitle(), productSize.getSize());
        cartItems.stream().forEach(s -> s.getCart().removeCartItem(s));

        productOption.removeProductSize(productSize);

        productOptionRepository.save(productOption);
        productSizeRepository.delete(productSize);
        cartItemRepository.deleteAll(cartItems);

        return "Successfully deleted product size!";
    }

    /**
     * Add brand
     *
     * @param addBrandRequest - JSON body
     * @return
     */

    public String addBrand(AddBrandRequest addBrandRequest) {
        if (brandRepository.existsByName(addBrandRequest.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Brand with same name already exists");
        }

        String imageName = addBrandRequest.getName() + "." + addBrandRequest.getImage().getOriginalFilename().split("\\.")[1];

        try {
            Files.copy(addBrandRequest.getImage().getInputStream(), Paths.get("src\\main\\resources\\static\\Brands\\" + imageName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding brand!");
        }

        brandRepository.save(new Brand(addBrandRequest.getName(), imageName));
        return "Successfully added brand";
    }

    /**
     * Update brand
     *
     * @param id   - Brand Id
     * @param name - New brand name
     * @return
     */

    public String updateBrand(Integer id, String name) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found!"));

        if (name.equals(brand.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }

        new File("src\\main\\resources\\static\\Brands\\" + brand.getImage()).renameTo(new File("src\\main\\resources\\static\\Brands\\" + name + "." + brand.getImage().split("\\.")[1]));

        brand.setName(name);
        brand.setImage(name + "." + brand.getImage().split("\\.")[1]);

        brandRepository.save(brand);
        return "Successfully changed brand name";
    }

    /**
     * Delete brand
     *
     * @param id - Brand Id
     * @return
     */

    public String deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found!"));

        List<Product> products = productRepository.findAllByBrand(brand);
        products.stream().forEach(p -> p.setBrand(null));

        new File("src\\main\\resources\\static\\Brands\\" + brand.getImage()).delete();

        productRepository.saveAll(products);
        brandRepository.delete(brand);

        return "Sucessfully deleted brand!";
    }

    /**
     * Change Image of Brand
     *
     * @param id   - Brand Id
     * @param file - New Image
     * @return
     */

    public String changeImageOfBrand(Integer id, MultipartFile file) {
        Brand brand = brandRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found!"));

        try {
            Files.copy(file.getInputStream(), Paths.get("src\\main\\resources\\static\\Brands\\" + brand.getImage()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while changing image of brand!");
        }

        return "Successfully changed image!";
    }

    /**
     * Add new category
     *
     * @param manageCategory - JSON body
     * @return
     */

    public String addCategory(ManageCategory manageCategory) {
        Category parent = null;

        if (manageCategory.getParrentId() != null) {
            parent = categoryRepository.findById(manageCategory.getParrentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
            if (categoryRepository.existsByParent_IdAndName(manageCategory.getParrentId(), manageCategory.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sub category with this name already exists!");
            }
        } else {
            if (categoryRepository.existsByNameAndParentIsNull(manageCategory.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists!");
            }
        }

        categoryRepository.save(new Category(manageCategory.getName(), parent));
        return "Successfully added category!";
    }

    /**
     * Update category
     *
     * @param id             - Category Id
     * @param manageCategory - JSON body
     * @return
     */

    public String updateCategory(Integer id, ManageCategory manageCategory) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        if (category.getName().equals(manageCategory.getName())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You haven't updated anything!");
        }
        if (manageCategory.getParrentId() != null) {
            if (categoryRepository.existsByParent_IdAndName(manageCategory.getParrentId(), manageCategory.getName()) && !category.getName().equals(manageCategory.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Sub category with this name already exists!");
            }
        } else {
            if (categoryRepository.existsByNameAndParentIsNull(manageCategory.getName()) && !category.getName().equals(manageCategory.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with this name already exists!");
            }
        }

        category.setName(manageCategory.getName());
        categoryRepository.save(category);
        return "Successfully updated category!";
    }

    /**
     * Delete category
     *
     * @param id - Category Id
     * @return
     */

    public String deleteCategory(Integer id) {
        List<Product> products = productRepository.findByCategory(id);
        products.stream().forEach(s -> s.setCategory(null));

        productRepository.saveAll(products);
        categoryRepository.deleteAll(categoryRepository.findChildCategories(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found!")));
        return "Successfully deleted category!";
    }

    /**
     * Discount products
     *
     * @param discountRequest - JSON body
     * @return
     */

    public String discountProducts(DiscountRequest discountRequest) {
        switch (discountRequest.getDiscountBy()) {
            case Brand:
                discountByBrand(discountRequest);
                break;
            case Category:
                discountByCategory(discountRequest);
                break;
            case Product:
                discountByProduct(discountRequest);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occured");
        }

        return "Successfully discounted products!";
    }

    public void discountByBrand(DiscountRequest discountRequest) {
        List<ProductOption> productOptions = productOptionRepository.findAllByProduct_Brand_Name(discountRequest.getValue()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found!"));
        productOptions.stream().forEach(p -> p.setDiscount(p.getPrice().subtract((p.getPrice().divide(new BigDecimal(100))).multiply(new BigDecimal(100).subtract(discountRequest.getDiscount())))));
        productOptionRepository.saveAll(productOptions);
    }

    private void discountByCategory(DiscountRequest discountRequest) {
        List<ProductOption> productOptions = productOptionRepository.findByCategory(Integer.parseInt(discountRequest.getValue())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand not found!"));
        productOptions.stream().forEach(p -> p.setDiscount(p.getPrice().subtract((p.getPrice().divide(new BigDecimal(100))).multiply(new BigDecimal(100).subtract(discountRequest.getDiscount())))));
        productOptionRepository.saveAll(productOptions);
    }

    public void discountByProduct(DiscountRequest discountRequest) {
        List<ProductOption> productOptions = productOptionRepository.findAllByProduct_Title(discountRequest.getValue()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found!"));
        productOptions.stream().forEach(s -> s.setDiscount(s.getPrice().subtract((s.getPrice().divide(new BigDecimal(100))).multiply(new BigDecimal(100).subtract(discountRequest.getDiscount())))));
        productOptionRepository.saveAll(productOptions);
    }


    /**
     * Create coupon
     *
     * @param coupon - JSON body
     * @return
     */

    public String createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Coupon with this code already exists!");
        }

        if (coupon.getDiscountType().equals(DISCOUNT.DELIVERY) && coupon.getDiscount() != null) {
            coupon.setDiscount(null);
        } else if (coupon.getDiscountType().equals(DISCOUNT.PERCENTAGE) && coupon.getDiscount() == null || coupon.getDiscountType().equals(DISCOUNT.TOTAL) && coupon.getDiscount() == null) {
            coupon.setDiscount(new BigDecimal(0));
        }

        if (coupon.getDiscountType().equals(DISCOUNT.PERCENTAGE) && coupon.getDiscount().compareTo(new BigDecimal(100)) == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Percentage discount can not be higher than 100%!");
        }

        couponRepository.save(coupon);
        return "Successfully created coupon!";
    }

    /**
     * Enable or disable coupon
     *
     * @param id - Coupon Id
     * @return
     */

    public String enableDisableCoupon(Integer id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        coupon.setEnabled(!coupon.getEnabled());

        if (!coupon.getEnabled()) {
            List<Cart> cartsThatAreUsingCoupon = cartRepository.findAllByCoupon_Id(id);
            cartsThatAreUsingCoupon.stream().forEach(s -> s.setCoupon(null));
            cartRepository.saveAll(cartsThatAreUsingCoupon);
        }

        couponRepository.save(coupon);
        return "Successfully updated coupon!";
    }

    /**
     * Add new image to carousel
     *
     * @param request - JSON body
     * @return
     */

    public String addNewCarouselImage(AddCarouselRequest request) {
        String imageName = UUID.randomUUID() + "." + request.getImage().getOriginalFilename().split("\\.")[1];

        try {
            Files.copy(request.getImage().getInputStream(), Paths.get("src\\main\\resources\\static\\Carousel\\" + imageName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while adding brand!");
        }

        carouselImageRepository.save(new CarouselImage(request.getText(), request.getLink(), imageName));
        return "Successfully added new carousel!";
    }

    /**
     * Update carousel image
     *
     * @param id      - Carousel id
     * @param request - JSON Body
     * @return
     */

    public String updateCarouselImage(Integer id, CarouselImage request) {
        CarouselImage carousel = carouselImageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        if (carousel.equals(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You haven't updated anything!");
        }
        carousel.setLink(request.getLink());
        carousel.setText(request.getText());

        carouselImageRepository.save(carousel);
        return "Successfully updated carousel!";
    }

    /**
     * Delete carousel image
     *
     * @param id - Carousel id
     * @return
     */

    public String deleteCarouselImage(Integer id) {
        CarouselImage carousel = carouselImageRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        new File("src\\main\\resources\\static\\Carousel\\" + carousel.getImage()).delete();
        carouselImageRepository.delete(carousel);
        return "Successfully deleted carousel!";
    }

    /**
     * Add alert
     *
     * @param request - JSON Body
     * @return
     */

    public String addAlert(Alert request) {
        alertRepository.save(request);
        return "Successfully added new alert!";
    }

    /**
     * Update alert
     *
     * @param id      - Alert id
     * @param request - JSON Body
     * @return
     */

    public String updateAlert(Integer id, Alert request) {
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        if (alert.equals(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You haven't updated anything!");
        }

        alert.setText(request.getText());
        alert.setColor(request.getColor());
        alertRepository.save(alert);
        return "Successfully updated new alert!";
    }

    /**
     * Delete alert
     *
     * @param id - Alert Id
     * @return
     */

    public String deleteAlert(Integer id) {
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        alertRepository.delete(alert);
        return "Successfully deleted alert!";
    }

    /**
     * Change price of delivery
     *
     * @param id    - Delivery id
     * @param price - New price
     * @return
     */

    public String changePriceofDelivery(Integer id, BigDecimal price) {
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        if (price.compareTo(delivery.getPrice()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You haven't updated anything!");
        }
        delivery.setPrice(price);
        deliveryRepository.save(delivery);
        return "Successfully changed price of delivery!";
    }

    /**
     * Change price of delivery
     *
     * @param id    - Delivery id
     * @param price - New price
     * @return
     */

    public String changePriceOfpayment(Integer id, BigDecimal price) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        if (price.compareTo(payment.getPrice()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You haven't updated anything!");
        }
        payment.setPrice(price);
        paymentRepository.save(payment);
        return "Successfully changed price of payment!";
    }

    public DashboardDTO dashboardData() {
        List<Integer> users = new ArrayList<>();
        List<Integer> orders = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            users.add(0, userRepository.findAllByRegistrationDay(LocalDateTime.now().minusDays(i).getDayOfMonth(), LocalDateTime.now().minusDays(i).getMonth().getValue(), LocalDateTime.now().minusDays(i).getYear()).size());
            orders.add(0, orderRepository.findAllByCreatedDay(LocalDateTime.now().minusDays(i).getDayOfMonth(), LocalDateTime.now().minusDays(i).getMonth().getValue(), LocalDateTime.now().minusDays(i).getYear()).size());
        }

        return new DashboardDTO(userRepository.findAll().size(), productOptionRepository.findAll().size(), orderRepository.findAllByOrderState(OrderState.COMPLETED).size(), orders, users);
    }

    public Order getOrder(Integer id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
    }

    public List<String> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(e -> e.getName()).collect(Collectors.toList());
    }

    public Page<User> findAllUsers(String search, Pageable pageable) {
        Page<User> x = userRepository.findAllByNameContainingOrEmailContaining(search, pageable);
        x.getContent().forEach(s -> {
            s.setCart(null);
        });
        return x;
    }

    public Page<Order> findAllOrders(String search, Pageable pageable) {
        return orderRepository.findAllByNameContainingOrEmailContainingOrEmailContainingOrTelNumberContaining(search, pageable);
    }

    public Page<ProductOption> findProductOptions(String search, Pageable pageable) {
        return productOptionRepository.findAllBySearch(search, pageable);
    }

    public List<String> getUserEmails(String email) {
        List<User> users = userRepository.findAllByEmailContaining(email);
        return users.stream().map(e -> e.getEmail()).collect(Collectors.toList());
    }

    public List<ProductDto> getProducts(String product) {
        List<Product> products = productRepository.findAllByTitleContaining(product);
        return products.stream().map(e -> new ProductDto(e)).collect(Collectors.toList());
    }

    public List<String> getBrands(String brand) {
        List<Brand> brands = brandRepository.findAllByNameContaining(brand);
        return brands.stream().map(e -> e.getName()).collect(Collectors.toList());
    }

    public ProductOption getProductOption(String title, String underTitle) {
        return productOptionRepository.findByProduct_TitleAndUnderTitle(title, underTitle).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    public Page<Coupon> findAllCoupons(String search, Pageable pageable) {
        return couponRepository.findAllBySearch(search, pageable);
    }

    public Page<Order> findUsersOrders(Integer id, Pageable pageable) {
        return orderRepository.findAllByUser_Id(id, pageable);
    }

    public List<Brand> findAllBrands() {
        return brandRepository.findAll();
    }


}
