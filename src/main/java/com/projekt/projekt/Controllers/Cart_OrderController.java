package com.projekt.projekt.Controllers;


import com.projekt.projekt.Enums.DELIVERY;
import com.projekt.projekt.Enums.PAYMENT;
import com.projekt.projekt.Models.Ecommerce.Cart;
import com.projekt.projekt.Models.Ecommerce.Delivery;
import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Models.Ecommerce.Payment;
import com.projekt.projekt.Responses.OrderDto;
import com.projekt.projekt.Requests.AddToCartRequest;
import com.projekt.projekt.Requests.ChargeRequest;
import com.projekt.projekt.Services.CartService;
import com.projekt.projekt.Services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;


@CrossOrigin
@RestController
public class Cart_OrderController {

    private OrderService orderService;
    private CartService cartService;

    public Cart_OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @PostMapping("/orders/{orderId}/checkout/paypal/create")
    public String createPayment(@PathVariable Integer orderId) {
        return orderService.createPayment(orderId);
    }

    @GetMapping("/orders/{orderId}/checkout/paypal/capture/{paypalId}")
    public String capturePayment(@PathVariable Integer orderId, @PathVariable String paypalId) {
        return orderService.capturePayment(orderId, paypalId);
    }

    @PostMapping("/orders/{orderId}/checkout/stripe")
    public String checkout_stripe(@PathVariable Integer orderId, @RequestBody ChargeRequest chargeRequest) {
        return orderService.chargeStripe(orderId, chargeRequest);
    }

    @PostMapping("/orders")
    private String order(@Valid @RequestBody Order order, BindingResult result, @RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestParam(value = "update", required = false) Boolean shouldUpdate) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, result.getFieldError().getDefaultMessage());
        }
        return orderService.createOrder(order, token, cartId, shouldUpdate);
    }

    @GetMapping("/orders/{id}")
    private OrderDto getOrder(@PathVariable Integer id) {
        return orderService.findOrderById(id);
    }

    @PutMapping("/orders/{id}")
    public String cancelOrder(@RequestHeader(value = "token", required = false) String token, @PathVariable Integer id) {
        return orderService.cancelOrder(token, id);
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @Valid @RequestBody AddToCartRequest addToCartRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, result.getFieldError().getDefaultMessage());
        }
        return cartService.addToCart(cartId, addToCartRequest, token);
    }

    @DeleteMapping("/cart-item")
    public String deleteCartItem(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestBody AddToCartRequest deleteCIrequest) {
        return cartService.deleteCartItem(cartId, token, deleteCIrequest);
    }

    @PutMapping("/cart-item")
    public String updateCartItem(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestBody AddToCartRequest updateCIrequest) {
        return cartService.updateCartItem(cartId, token, updateCIrequest);
    }

    @PutMapping("/delivery")
    public String setDelivery(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestParam("delivery") DELIVERY delivery) {
        return cartService.setDelivery(cartId, token, delivery);
    }

    @PutMapping("/payment")
    public String setPayment(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestParam("payment") PAYMENT payment) {
        return cartService.setPayment(cartId, token, payment);
    }

    @GetMapping("/cart")
    public Cart getCart(@CookieValue(value = "cart", required = false) String cartId, @RequestHeader(value = "token", required = false) String token, HttpServletRequest request, HttpServletResponse response) {
        return cartService.checkForCartCookie(request, response, cartId, token);
    }

    @PutMapping("/coupon")
    public String setCoupon(@RequestHeader(value = "token", required = false) String token, @CookieValue(value = "cart", required = false) String cartId, @RequestParam String coupon) {
        return cartService.setCoupon(cartId, token, coupon);
    }

    @GetMapping("/payments")
    public List<Payment> payments() {
        return cartService.findPayments();
    }

    @GetMapping("/deliveries")
    public List<Delivery> deliveries() {
        return cartService.findDeliveries();
    }
}
