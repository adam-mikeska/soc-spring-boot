package com.projekt.projekt.Services;

import com.projekt.projekt.Enums.DELIVERY;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Enums.PAYMENT;
import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Repositories.Ecommerce.*;
import com.projekt.projekt.Repositories.UserRepository;
import com.projekt.projekt.Requests.AddToCartRequest;
import com.projekt.projekt.Security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CartService {

    final private ProductOptionRepository productOptionRepository;
    final private CartRepository cartRepository;
    final private CartItemRepository cartItemRepository;
    final private UserRepository userRepository;
    final private JwtUtil jwtUtil;
    final private ProductSizeRepository productSizeRepository;
    final private DeliveryRepository deliveryRepository;
    final private PaymentRepository paymentRepository;
    final private Cart_OrderPaymentRepository cart_orderPaymentRepository;
    final private Cart_OrderDeliveryRepository cart_orderDeliveryRepository;
    final private CouponRepository couponRepository;

    public static final String myCookieName = "cart";

    public CartService(ProductOptionRepository productOptionRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, JwtUtil jwtUtil, ProductSizeRepository productSizeRepository, DeliveryRepository deliveryRepository, PaymentRepository paymentRepository, Cart_OrderPaymentRepository cart_orderPaymentRepository, Cart_OrderDeliveryRepository cart_orderDeliveryRepository, CouponRepository couponRepository) {
        this.productOptionRepository = productOptionRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.productSizeRepository = productSizeRepository;
        this.deliveryRepository = deliveryRepository;
        this.paymentRepository = paymentRepository;
        this.cart_orderPaymentRepository = cart_orderPaymentRepository;
        this.cart_orderDeliveryRepository = cart_orderDeliveryRepository;
        this.couponRepository = couponRepository;
    }

    /**
     * Provide cart
     * @param token - User's token
     * @param cartId - Cart Id
     * @return
     */

    public Cart provideCart(String token, String cartId) {
        token = jwtUtil.shortToken(token);

        User user = null;
        Cart cart = null;

        if (token != null) {
            user = userRepository.findByEmail(jwtUtil.extractEmail(token));
        }

        if (cartId != null && user == null) {
            if (cartRepository.existsById(cartId)) {
                cart = cartRepository.findById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
            }
        }

        if (cart == null && user == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cart is not present.");
        }

        return (user == null ? cart : user.getCart());
    }

    /***
     * Create CART and its cookie if user is not present
     * @param request - Get cookies
     * @param servletResponse - Create cookie
     * @param id - Cart Id
     * @param token - User's token
     * @return
     */

    public Cart checkForCartCookie(HttpServletRequest request, HttpServletResponse servletResponse, String id, String token) {
        Boolean cartCookieExists = false;

        if (token != null) {
            return null;
        }

        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(myCookieName)) {
                    cartCookieExists = true;
                    break;
                }
            }
            if (cartCookieExists == false) {
                return createCookie(servletResponse);
            } else {
                if (!cartRepository.existsById(id)) {
                    Cookie cookie = new Cookie(myCookieName, null);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(0);
                    servletResponse.addCookie(cookie);
                    return createCookie(servletResponse);
                } else {
                    return cartRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Error"));
                }
            }
        } else {
            return createCookie(servletResponse);
        }
    }

    public Cart createCookie(HttpServletResponse servletResponse) {
        Cart cart = new Cart();
        cartRepository.save(cart);

        Cookie newCookie = new Cookie(myCookieName, cart.getId());
        newCookie.setPath("/");
        newCookie.setHttpOnly(true);
        newCookie.setMaxAge(86400);

        servletResponse.addCookie(newCookie);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                Cart_OrderDelivery cart_orderDelivery = cart_orderDeliveryRepository.findByCartId(cart.getId());
                Cart_OrderPayment cart_orderPayment = cart_orderPaymentRepository.findByCartId(cart.getId());

                cartRepository.delete(cart);
                cart_orderDeliveryRepository.delete(cart_orderDelivery);
                cart_orderPaymentRepository.delete(cart_orderPayment);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, 1, TimeUnit.DAYS);

        return cart;
    }

    /**
     * Add To Cart
     * @param cartId - Cart Id
     * @param addToCartRequest - JSON body
     * @param token - User's token
     * @return
     */

    public String addToCart(String cartId, AddToCartRequest addToCartRequest, String token) {
        Cart cart = provideCart(token, cartId);
        proceedAddToCart(addToCartRequest, cart);
        return "Sucessfully added to cart.";
    }

    public void proceedAddToCart(AddToCartRequest addToCartRequest, Cart cart) {
        Boolean cartItemExists = cartItemRepository.existsCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(addToCartRequest.getProduct(), addToCartRequest.getUnderTitle(), addToCartRequest.getSize(), cart.getId());
        CartItem cartItem = getCartItem(addToCartRequest, cartItemExists, cart);
        ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(addToCartRequest.getSize(), addToCartRequest.getProduct(), addToCartRequest.getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size was not found!"));

        if (productSize.getOnStock() < addToCartRequest.getQuantity() + (cartItemExists ? cartItem.getQuantity() : 0)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough of product in stock! In stock: " + productSize.getOnStock());
        }

        if (cartItemExists) {
            cartItem.setQuantity(cartItem.getQuantity() + addToCartRequest.getQuantity());
            cart.checkForCartPrice();
        } else {
            cart.addCartItem(cartItem);
        }

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    public CartItem getCartItem(AddToCartRequest addToCartRequest, Boolean cartItemExists, Cart cart) {
        if (cartItemExists) {
            return cartItemRepository.findCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(addToCartRequest.getProduct(), addToCartRequest.getUnderTitle(), addToCartRequest.getSize(), cart.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item is not present anymore!"));
        } else {
            return new CartItem(addToCartRequest.getSize(), addToCartRequest.getQuantity(), productOptionRepository.findByProduct_TitleAndUnderTitleAndEnabledIsTrue(addToCartRequest.getProduct(), addToCartRequest.getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product was not found!")), cart);
        }
    }

    /**
     * Delete Cart item
     * @param cartId - Cart id if user is anonymous
     * @param token - TOKEN if user is authentificated
     * @param deleteCIrequest - JSON BODY
     * @return
     */

    public String deleteCartItem(String cartId, String token,AddToCartRequest deleteCIrequest) {
        Cart cart = provideCart(token, cartId);
        CartItem cartItem = cartItemRepository.findCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(deleteCIrequest.getProduct(), deleteCIrequest.getUnderTitle(), deleteCIrequest.getSize(), cart.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item is not present anymore!"));

        cart.removeCartItem(cartItem);

        cartRepository.save(cart);
        cartItemRepository.delete(cartItem);

        return "Sucessfully deleted cart item!";
    }

    /**
     * Update cart item
     * @param cartId - Cart id if user is anonymous
     * @param token - TOKEN if user is authentificated
     * @param updateCIrequest - JSON BODY
     * @return
     */

    public String updateCartItem(String cartId,String token, AddToCartRequest updateCIrequest) {
        Cart cart = provideCart(token, cartId);

        CartItem cartItem = cartItemRepository.findCartItemByProductOption_Product_titleAndProductOption_UnderTitleAndProductSizeAndCartId(updateCIrequest.getProduct(), updateCIrequest.getUnderTitle(), updateCIrequest.getSize(), cart.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item is not present anymore!"));
        ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(updateCIrequest.getSize(), updateCIrequest.getProduct(), updateCIrequest.getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size not found!"));

        if (productSize.getOnStock() < updateCIrequest.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough of product in stock! In stock: " + productSize.getOnStock());
        }
        if (cartItem.getQuantity() == updateCIrequest.getQuantity()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated cartitem!");
        }
        proceedUpdateCartItem(cart, cartItem, updateCIrequest.getQuantity());

        return "Sucessfully updated cart item!";

    }

    public void proceedUpdateCartItem(Cart cart, CartItem cartItem, Integer quantity) {
        if (quantity < 1) {
            cart.removeCartItem(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cart.checkForCartPrice();
        }

        if (quantity < 1) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
    }

    /**
     * Set Delivery
     * @param cartId - Cart Id
     * @param token - User's token
     * @param delivery - Delivery
     * @return
     */

    public String setDelivery(String cartId, String token, DELIVERY delivery) {
        Cart cart = provideCart(token, cartId);
        proceedSetDelivery(cart, delivery);
        return "Success!";
    }

    public void proceedSetDelivery(Cart crt, DELIVERY delivery) {
        Delivery deliveryENT = deliveryRepository.findByTitle(delivery).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery method not found"));

        BigDecimal deliveryPrice = (crt.getCoupon() != null && crt.getCoupon().getDiscountType().equals(DISCOUNT.DELIVERY) ? new BigDecimal(0) : deliveryENT.getPrice());

        if (crt.getCart_orderDelivery() != null) {
            Cart_OrderDelivery cart_orderDelivery = cart_orderDeliveryRepository.findByCartId(crt.getId());
            if (delivery == crt.getCart_orderDelivery().getDelivery()) {
                crt.setCart_orderDelivery(null);
                cart_orderDeliveryRepository.delete(cart_orderDelivery);
            } else {
                cart_orderDelivery.setPrice(deliveryPrice);
                cart_orderDelivery.setDelivery(delivery);
                cart_orderDeliveryRepository.save(cart_orderDelivery);
            }

        } else {
            Cart_OrderDelivery cart_orderDelivery = new Cart_OrderDelivery(delivery, deliveryPrice, crt.getId(), deliveryENT);
            cart_orderDeliveryRepository.save(cart_orderDelivery);
            crt.setCart_orderDelivery(cart_orderDelivery);
        }

        cartRepository.save(crt);
    }

    /**
     * Set payment
     * @param cartId - Cart Id
     * @param token - User's token
     * @param payment - Payment
     * @return
     */

    public String setPayment(String cartId, String token, PAYMENT payment) {
        Cart cart = provideCart(token, cartId);
        proceedSetPayment(cart, payment);
        return "Success!";
    }

    public void proceedSetPayment(Cart crt, PAYMENT payment) {
        Payment paymentENT = paymentRepository.findByTitle(payment).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment method not found!"));

        if (crt.getCart_orderPayment() != null) {
            Cart_OrderPayment cart_orderPayment = cart_orderPaymentRepository.findByCartId(crt.getId());
            if (payment == crt.getCart_orderPayment().getPayment()) {
                crt.setCart_orderPayment(null);
                cart_orderPaymentRepository.delete(cart_orderPayment);
            } else {
                cart_orderPayment.setPrice(paymentENT.getPrice());
                cart_orderPayment.setPayment(payment);
                cart_orderPaymentRepository.save(cart_orderPayment);
            }
        } else {
            Cart_OrderPayment cart_orderPayment = new Cart_OrderPayment(payment, paymentENT.getPrice(), crt.getId(), paymentENT);
            cart_orderPaymentRepository.save(cart_orderPayment);
            crt.setCart_orderPayment(cart_orderPayment);
        }

        cartRepository.save(crt);
    }

    /**
     * Set Coupon
     * @param cartId - Cart Id
     * @param token - User's token
     * @param code - Coupon code
     * @return
     */

    public String setCoupon(String cartId, String token, String code) {
        Cart cart = provideCart(token, cartId);

        Coupon coupon = couponRepository.findByCodeAndEnabledIsTrue(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found!"));

        Boolean shouldSetNewCoupon = cart.getCoupon() != coupon;

        if (cart.getCoupon() != null) {
            removeCurentCoupon(cart);
        }

        BigDecimal totalPriceWithoutPaymentAndDelivery = cart.getCartItems().stream()
                .reduce(BigDecimal.ZERO, (bd, item) -> bd.add((item.getProductOption().getPrice().subtract(item.getProductOption().getDiscount()))
                        .multiply(BigDecimal.valueOf(item.getQuantity()))), BigDecimal::add);

        if (totalPriceWithoutPaymentAndDelivery.compareTo(coupon.getMinCartPrice()) == -1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Minimum cart price has to be atleast " + coupon.getMinCartPrice() + "€");
        }

        if (shouldSetNewCoupon) {
            setNewCoupon(cart, coupon);
        }

        cartRepository.save(cart);

        return "Successfully " + (shouldSetNewCoupon ? "set" : "removed") + " coupon!";
    }

    public void removeCurentCoupon(Cart cart) {
        if (cart.getCoupon().getDiscountType().equals(DISCOUNT.DELIVERY) && cart.getCart_orderDelivery() != null) {
            cart.getCart_orderDelivery().setPrice(deliveryRepository.findByTitle(cart.getCart_orderDelivery().getDelivery()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery method not found!")).getPrice());
            cart_orderDeliveryRepository.save(cart.getCart_orderDelivery());
        }
        cart.setCoupon(null);
    }

    public void setNewCoupon(Cart cart, Coupon coupon) {
        if (coupon.getDiscountType().equals(DISCOUNT.DELIVERY)) {
            deliveryCoupon(cart, coupon);
        } else if (coupon.getDiscountType().equals(DISCOUNT.PERCENTAGE)) {
            percentageCoupon(cart, coupon);
        } else if (coupon.getDiscountType().equals(DISCOUNT.TOTAL)) {
            totalPriceCoupon(cart, coupon);
        }
    }

    public void deliveryCoupon(Cart cart, Coupon coupon) {
        if (cart.getCart_orderDelivery() != null) {
            cart.getCart_orderDelivery().setPrice(new BigDecimal(0));
            cart_orderDeliveryRepository.save(cart.getCart_orderDelivery());
        }
        cart.setCoupon(coupon);
    }

    private void percentageCoupon(Cart cart, Coupon coupon) {
        cart.setCoupon(coupon);
    }

    private void totalPriceCoupon(Cart cart, Coupon coupon) {
        BigDecimal totalPriceWithoutPaymentAndDelivery = cart.getCartItems().stream().reduce(BigDecimal.ZERO, (bd, item) -> bd.add((item.getProductOption().getPrice().subtract(item.getProductOption().getDiscount())).multiply(BigDecimal.valueOf(item.getQuantity()))), BigDecimal::add);

        if (totalPriceWithoutPaymentAndDelivery.compareTo(coupon.getDiscount()) == 1) {
            cart.setCoupon(coupon);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cart total price has to be higher than discount!");
        }
    }

    public List<Payment> findPayments() {
        return paymentRepository.findAll();
    }

    public List<Delivery> findDeliveries() {
        return deliveryRepository.findAll();
    }

}
