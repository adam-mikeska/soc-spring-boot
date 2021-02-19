package com.projekt.projekt.Models;

import com.projekt.projekt.Enums.DELIVERY;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Enums.GENDER;
import com.projekt.projekt.Enums.PAYMENT;
import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Repositories.*;
import com.projekt.projekt.Repositories.Ecommerce.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DbInit implements CommandLineRunner {
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductOptionRepository productOptionRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ProductSizeRepository productSizeRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private CarouselImageRepository carouselRepository;
    @Autowired
    private AlertRepository alertRepository;

    @Override
    public void run(String... args)  {
        Coupon coupon = new Coupon("FREEDEL",null, DISCOUNT.DELIVERY,new BigDecimal(331));
        Coupon coupon1 = new Coupon("DOWN20P",new BigDecimal(20), DISCOUNT.PERCENTAGE,new BigDecimal(330));
        Coupon coupon2 = new Coupon("10EURCOUP",new BigDecimal(10), DISCOUNT.TOTAL,new BigDecimal(330));

        List<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon);
        coupons.add(coupon1);
        coupons.add(coupon2);

        couponRepository.saveAll(coupons);

        Payment payment = new Payment(new BigDecimal(0), PAYMENT.Online_bank_payment);
        Payment payment1 = new Payment(new BigDecimal(0),PAYMENT.Paypal);
        Payment payment2 = new Payment(new BigDecimal(3),PAYMENT.Cash_on_hand);
        List<Payment> payments = new ArrayList<>();
        payments.add(payment);
        payments.add(payment1);
        payments.add(payment2);
        paymentRepository.saveAll(payments);
        Delivery delivery = new Delivery(new BigDecimal(20), DELIVERY.Delivery_to_your_address);
        Delivery delivery1 = new Delivery(new BigDecimal(5),DELIVERY.Personal_visit);
        deliveryRepository.save(delivery);
        deliveryRepository.save(delivery1);

        Category forMan = new Category("For Man",null);
        Category forWoman = new Category("For Woman",null);

        Category slippers = new Category("Slippers",forMan);
        Category sneakers = new Category("Sneakers",forMan);
        Category limited = new Category("Limited",forMan);
        Category boots = new Category("Boots",limited);
        Category sandals = new Category("Sandals",limited);

        Category slippers1 = new Category("Slippers",forWoman);
        Category sneakers1 = new Category("Sneakers",forWoman);
        Category boots1 = new Category("Boots",forWoman);
        Category sandals1 = new Category("Sandals",forWoman);
        List<Category> categories = new ArrayList<>();
        categories.add(forMan);
        categories.add(forWoman);
        categories.add(limited);
        categories.add(slippers);
        categories.add(sneakers);
        categories.add(boots);
        categories.add(sandals);

        categories.add(slippers1);
        categories.add(sneakers1);
        categories.add(boots1);
        categories.add(sandals1);

        Brand brand1 = new Brand("Adidas","adidas.png");
        Brand brand2 = new Brand("Nike","nike.png");
        List<Brand> brands = new ArrayList<>();
        brands.add(brand1);
        brands.add(brand2);

        String loremIpsum = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
        Product product = new Product(brand1,"Yeezy",forMan);
        Product product1 = new Product(brand1,"Human race",limited);
        Product product2 = new Product(brand2,"Air jordan",slippers);
        Product product3 = new Product(brand2,"Dunky",sneakers);
        Product product5 = new Product(brand2,"Air Yeezy 2",boots);
        Product product6 = new Product(brand1,"Nmd",sandals);

        List<Product> products = new ArrayList<>();
        products.add(product);
        products.add(product1);
        products.add(product2);
        products.add(product3);
        products.add(product5);
        products.add(product6);

        ProductOption productOption = new ProductOption(product,new BigDecimal(300),new BigDecimal(0),"White",loremIpsum);
        ProductOption productOption1 = new ProductOption(product,new BigDecimal(300),new BigDecimal(30),"Black",loremIpsum);
        ProductOption productOption2 = new ProductOption(product,new BigDecimal(300),new BigDecimal(35),"Green",loremIpsum);
        ProductOption productOption3 = new ProductOption(product1,new BigDecimal(300),new BigDecimal(40),"Yellow",loremIpsum);
        ProductOption productOption4 = new ProductOption(product2,new BigDecimal(300),new BigDecimal(40),"Pink",loremIpsum);
        ProductOption productOption5 = new ProductOption(product3,new BigDecimal(100),new BigDecimal(25),"Grey",loremIpsum);
        ProductOption productOption6 = new ProductOption(product2,new BigDecimal(499.99),new BigDecimal(0),"Black Toe",loremIpsum);
        ProductOption productOption7 = new ProductOption(product2,new BigDecimal(999.99),new BigDecimal(300),"Tokyo",loremIpsum);
        ProductOption productOption8 = new ProductOption(product5,new BigDecimal(4999.99),new BigDecimal(1000),"Red October",loremIpsum);
        ProductOption productOption9 = new ProductOption(product6,new BigDecimal(299.99),new BigDecimal(50),"Bape",loremIpsum);

        List<ProductOption> productOptions = new ArrayList<>();
        productOptions.add(productOption);
        productOptions.add(productOption1);
        productOptions.add(productOption2);
        productOptions.add(productOption3);
        productOptions.add(productOption4);
        productOptions.add(productOption6);
        productOptions.add(productOption7);
        productOptions.add(productOption8);
        productOptions.add(productOption9);
        productOptions.add(productOption5);

        ProductImage productImage = new ProductImage("1.jpg",productOption);
        ProductImage productImage1 = new ProductImage("2.jpg",productOption);
        List<ProductImage> productImages = new ArrayList<>();
        productImages.add(productImage);
        productImages.add(productImage1);

        ProductImage productImage3 = new ProductImage("3.jpg",productOption1);
        ProductImage productImage4 = new ProductImage("4.jpg",productOption1);
        List<ProductImage> productImages1 = new ArrayList<>();
        productImages1.add(productImage3);
        productImages1.add(productImage4);

        ProductImage productImage5 = new ProductImage("5.jpg",productOption2);
        ProductImage productImage6 = new ProductImage("6.jpg",productOption2);
        List<ProductImage> productImages2 = new ArrayList<>();
        productImages2.add(productImage5);
        productImages2.add(productImage6);

        ProductImage productImage7 = new ProductImage("7.jpg",productOption3);
        ProductImage productImage8 = new ProductImage("8.jpg",productOption3);
        List<ProductImage> productImages3 = new ArrayList<>();
        productImages3.add(productImage7);
        productImages3.add(productImage8);

        ProductImage productImage9 = new ProductImage("9.jpg",productOption4);
        ProductImage productImage10 = new ProductImage("10.jpg",productOption4);
        List<ProductImage> productImages4 = new ArrayList<>();
        productImages4.add(productImage9);
        productImages4.add(productImage10);

        ProductImage productImage11 = new ProductImage("11.jpg",productOption5);
        ProductImage productImage12 = new ProductImage("12.jpg",productOption5);
        List<ProductImage> productImages5 = new ArrayList<>();
        productImages5.add(productImage11);
        productImages5.add(productImage12);

        ProductImage productImage13 = new ProductImage("13.jpg",productOption6);
        ProductImage productImage14 = new ProductImage("14.jpg",productOption6);
        List<ProductImage> productImages6 = new ArrayList<>();
        productImages6.add(productImage13);
        productImages6.add(productImage14);

        ProductImage productImage15 = new ProductImage("15.jpg",productOption7);
        ProductImage productImage16 = new ProductImage("16.jpg",productOption7);
        List<ProductImage> productImages7 = new ArrayList<>();
        productImages7.add(productImage15);
        productImages7.add(productImage16);

        ProductImage productImage17 = new ProductImage("17.jpg",productOption8);
        ProductImage productImage18 = new ProductImage("18.jpg",productOption8);
        List<ProductImage> productImages8 = new ArrayList<>();
        productImages8.add(productImage17);
        productImages8.add(productImage18);

        ProductImage productImage19 = new ProductImage("19.jpg",productOption9);
        ProductImage productImage20 = new ProductImage("20.jpg",productOption9);
        List<ProductImage> productImages9 = new ArrayList<>();
        productImages9.add(productImage19);
        productImages9.add(productImage20);

        ProductSize productSize = new ProductSize("40",20,productOption);
        ProductSize productSize1 = new ProductSize("41",20,productOption);
        ProductSize productSize2 = new ProductSize("42",20,productOption);
        ProductSize productSize3 = new ProductSize("43",20,productOption);
        List<ProductSize> productSizes = new ArrayList<>();
        productSizes.add(productSize);
        productSizes.add(productSize1);
        productSizes.add(productSize2);
        productSizes.add(productSize3);

        ProductSize productSize00 = new ProductSize("40",5,productOption1);
        ProductSize productSize11 = new ProductSize("41",20,productOption1);
        ProductSize productSize22 = new ProductSize("42",20,productOption1);
        ProductSize productSize33 = new ProductSize("43",20,productOption1);
        List<ProductSize> productSizez = new ArrayList<>();
        productSizez.add(productSize00);
        productSizez.add(productSize11);
        productSizez.add(productSize22);
        productSizez.add(productSize33);

        ProductSize productSize000 = new ProductSize("40",10,productOption2);
        ProductSize productSize111 = new ProductSize("41",20,productOption2);
        ProductSize productSize222 = new ProductSize("42",20,productOption2);
        ProductSize productSize333 = new ProductSize("43",20,productOption2);
        List<ProductSize> productSizess = new ArrayList<>();
        productSizess.add(productSize000);
        productSizess.add(productSize111);
        productSizess.add(productSize222);
        productSizess.add(productSize333);

        ProductSize productSize0000 = new ProductSize("40",20,productOption3);
        ProductSize productSize1111 = new ProductSize("41",20,productOption3);
        ProductSize productSize2222 = new ProductSize("42",20,productOption3);
        ProductSize productSize3333 = new ProductSize("43",20,productOption3);
        List<ProductSize> productSizesss = new ArrayList<>();
        productSizesss.add(productSize0000);
        productSizesss.add(productSize1111);
        productSizesss.add(productSize2222);
        productSizesss.add(productSize3333);

        ProductSize productSize00000 = new ProductSize("40",20,productOption4);
        ProductSize productSize11111 = new ProductSize("41",20,productOption4);
        ProductSize productSize22222 = new ProductSize("42",20,productOption4);
        ProductSize productSize33333 = new ProductSize("43",20,productOption4);
        List<ProductSize> productSizessss = new ArrayList<>();
        productSizessss.add(productSize00000);
        productSizessss.add(productSize11111);
        productSizessss.add(productSize22222);
        productSizessss.add(productSize33333);

        ProductSize productSize000000 = new ProductSize("40",20,productOption5);
        ProductSize productSize111111 = new ProductSize("41",20,productOption5);
        ProductSize productSize222222 = new ProductSize("42",20,productOption5);
        ProductSize productSize333333 = new ProductSize("43",20,productOption5);
        List<ProductSize> productSizesssss = new ArrayList<>();
        productSizesssss.add(productSize000000);
        productSizesssss.add(productSize111111);
        productSizesssss.add(productSize222222);
        productSizesssss.add(productSize333333);

        ProductSize productSize0000000 = new ProductSize("40",20,productOption6);
        ProductSize productSize1111111 = new ProductSize("41",20,productOption6);
        ProductSize productSize2222222 = new ProductSize("42",20,productOption6);
        ProductSize productSize3333333 = new ProductSize("43",20,productOption6);
        List<ProductSize> productSizessssss = new ArrayList<>();
        productSizessssss.add(productSize0000000);
        productSizessssss.add(productSize1111111);
        productSizessssss.add(productSize2222222);
        productSizessssss.add(productSize3333333);

        ProductSize productSize00000000 = new ProductSize("40",20,productOption7);
        ProductSize productSize11111111 = new ProductSize("41",20,productOption7);
        ProductSize productSize22222222 = new ProductSize("42",20,productOption7);
        ProductSize productSize33333333 = new ProductSize("43",20,productOption7);
        List<ProductSize> productSizesssssss = new ArrayList<>();
        productSizesssssss.add(productSize00000000);
        productSizesssssss.add(productSize11111111);
        productSizesssssss.add(productSize22222222);
        productSizesssssss.add(productSize33333333);

        ProductSize productSize000000000 = new ProductSize("40",20,productOption8);
        ProductSize productSize111111111 = new ProductSize("41",20,productOption8);
        ProductSize productSize222222222 = new ProductSize("42",20,productOption8);
        ProductSize productSize333333333 = new ProductSize("43",20,productOption8);
        List<ProductSize> productSizessssssss = new ArrayList<>();
        productSizessssssss.add(productSize000000000);
        productSizessssssss.add(productSize111111111);
        productSizessssssss.add(productSize222222222);
        productSizessssssss.add(productSize333333333);

        ProductSize productSize0000000000 = new ProductSize("40",20,productOption9);
        ProductSize productSize1111111111 = new ProductSize("41",20,productOption9);
        ProductSize productSize2222222222 = new ProductSize("42",20,productOption9);
        ProductSize productSize3333333333 = new ProductSize("43",20,productOption9);
        List<ProductSize> productSizesssssssss = new ArrayList<>();
        productSizesssssssss.add(productSize0000000000);
        productSizesssssssss.add(productSize1111111111);
        productSizesssssssss.add(productSize2222222222);
        productSizesssssssss.add(productSize3333333333);

        Role role1 = new Role("OWNER","*","blue");
        Role role2 = new Role("USER","","#000000");
        Role role3 = new Role("TRIAL_ADMIN","display_users,update_user","#800080");
        Role role5 = new Role("MODERATOR","","#008000");
        Role role6 = new Role("SUPPORT","","#a52a2a");
        Role role7 = new Role("EMPLOYEE","","#ffff00");

        Cart cart1 = new Cart();
        Cart cart2 = new Cart();
        Cart cart3 = new Cart();
        Cart cart4 = new Cart();
        Cart cart5 = new Cart();
        User admin = new User("Adam Krp", "adamikeska@gmail.com",role1,  passwordEncoder.encode("adam1234"), GENDER.Male,"+421904800696",cart1);
        User userr = new User("Adam Ads", "copbit1@gmail.com",role3,   passwordEncoder.encode("adam1234"),GENDER.Female,"+4219048006706",cart2);
        User user = new User("Peter Jajo",  "peterson@gmail.com",role2,  passwordEncoder.encode("adam1234"),GENDER.Male,"+4219048006704",cart3);
        User user2 = new User("Peter Amen", "peterson1@gmail.com",role3,  passwordEncoder.encode("adam1234"),GENDER.Female,"+4219048006724",cart4);
        User user3 = new User("Peter Dedo",  "peterson2@gmail.com",role3,  passwordEncoder.encode("adam1234"),GENDER.Male,"+4219048006714",cart5);
        List<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        roles.add(role3);
        roles.add(role5);
        roles.add(role6);
        roles.add(role7);
        List<Cart> carts = new ArrayList<>();
        carts.add(cart1);
        carts.add(cart2);
        carts.add(cart3);
        carts.add(cart4);
        carts.add(cart5);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(userr);
        users.add(admin);
        users.add(user2);
        users.add(user3);

        categoryRepository.saveAll(categories);

        brandRepository.saveAll(brands);
        productRepository.saveAll(products);
        productOptionRepository.saveAll(productOptions);

        productImageRepository.saveAll(productImages);
        productImageRepository.saveAll(productImages1);
        productImageRepository.saveAll(productImages2);
        productImageRepository.saveAll(productImages3);
        productImageRepository.saveAll(productImages4);
        productImageRepository.saveAll(productImages5);
        productImageRepository.saveAll(productImages6);
        productImageRepository.saveAll(productImages7);
        productImageRepository.saveAll(productImages8);
        productImageRepository.saveAll(productImages9);

        productSizeRepository.saveAll(productSizes);
        productSizeRepository.saveAll(productSizez);
        productSizeRepository.saveAll(productSizess);
        productSizeRepository.saveAll(productSizesss);
        productSizeRepository.saveAll(productSizessss);
        productSizeRepository.saveAll(productSizesssss);
        productSizeRepository.saveAll(productSizessssss);
        productSizeRepository.saveAll(productSizesssssss);
        productSizeRepository.saveAll(productSizessssssss);
        productSizeRepository.saveAll(productSizesssssssss);

        productOption.setProductSizes(productSizes);
        productOption1.setProductSizes(productSizez);
        productOption2.setProductSizes(productSizess);
        productOption3.setProductSizes(productSizesss);
        productOption4.setProductSizes(productSizessss);
        productOption5.setProductSizes(productSizesssss);
        productOption6.setProductSizes(productSizessssss);
        productOption7.setProductSizes(productSizesssssss);
        productOption8.setProductSizes(productSizessssssss);
        productOption9.setProductSizes(productSizesssssssss);

        productOption.setProductImages(productImages);
        productOption1.setProductImages(productImages1);
        productOption2.setProductImages(productImages2);
        productOption3.setProductImages(productImages3);
        productOption4.setProductImages(productImages4);
        productOption5.setProductImages(productImages5);
        productOption6.setProductImages(productImages6);
        productOption7.setProductImages(productImages7);
        productOption8.setProductImages(productImages8);
        productOption9.setProductImages(productImages9);

        productRepository.saveAll(products);
        productOptionRepository.saveAll(productOptions);
        roleRepository.saveAll(roles);
        cartRepository.saveAll(carts);
        userRepository.saveAll(users);
        cart1.setUser(admin);
        cart2.setUser(userr);
        cart3.setUser(user);
        cart4.setUser(user2);
        cart5.setUser(user3);
        cartRepository.saveAll(carts);

        CarouselImage carousel = new CarouselImage("<p class=h2>-20% sale for every Nike sneakers</p><p class=h4>Promocode FREE20</p>","/category/For%20Man","car1.jpg");
        CarouselImage carousel1 = new CarouselImage("<p class=h2>-20% sale for every Nike sneakers</p><p class=h4>Promocode FREE20</p>","/category/For%20Man","car2.jpg");
        CarouselImage carousel2 = new CarouselImage("<p class=h2>-20% sale for every Nike sneakers</p><p class=h4>Promocode FREE20</p>","/category/For%20Man","car3.jpg");

        List<CarouselImage> carousels = new ArrayList<>();
        carousels.add(carousel);
        carousels.add(carousel1);
        carousels.add(carousel2);
        carouselRepository.saveAll(carousels);

        Alert alert = new Alert(" <strong>Holy guacamole!</strong> You should check in on some of those fields below.","rgba(31,227,221,0.39)");
        alertRepository.save(alert);
    }
}
