package com.projekt.projekt.Services;

import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Enums.OrderState;
import com.projekt.projekt.Enums.PAYMENT;
import com.projekt.projekt.Models.Ecommerce.*;
import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Responses.OrderDto;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Repositories.Ecommerce.*;
import com.projekt.projekt.Repositories.UserRepository;
import com.projekt.projekt.Requests.ChargeRequest;
import com.projekt.projekt.Requests.SendEmailRequest;
import com.projekt.projekt.Security.JwtUtil;

import com.stripe.exception.*;
import com.stripe.model.Charge;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class OrderService {

    final private ProductSizeRepository productSizeRepository;
    final private CartItemRepository cartItemRepository;
    final private OrderItemRepository orderItemRepository;
    final private OrderRepository orderRepository;
    final private CartRepository cartRepository;
    final private JwtUtil jwtUtil;
    final private UserRepository userRepository;
    final private PayPalClient payPalClient;
    final private StripeService stripeService;
    final private Cart_OrderPaymentRepository cart_orderPaymentRepository;
    final private Cart_OrderDeliveryRepository cart_orderDeliveryRepository;
    final private ProductOptionRepository productOptionRepository;
    final private MailService mailService;
    @Value("${frontend}")
    private String FRONT_END;

    public OrderService(ProductSizeRepository productSizeRepository, CartItemRepository cartItemRepository, OrderItemRepository orderItemRepository, OrderRepository orderRepository, CartRepository cartRepository, JwtUtil jwtUtil, UserRepository userRepository, PayPalClient payPalClient, StripeService stripeService, Cart_OrderPaymentRepository cart_orderPaymentRepository, Cart_OrderDeliveryRepository cart_orderDeliveryRepository, ProductOptionRepository productOptionRepository, MailService mailService) {
        this.productSizeRepository = productSizeRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.payPalClient = payPalClient;
        this.stripeService = stripeService;
        this.cart_orderPaymentRepository = cart_orderPaymentRepository;
        this.cart_orderDeliveryRepository = cart_orderDeliveryRepository;
        this.productOptionRepository = productOptionRepository;
        this.mailService = mailService;
    }

    /**
     * Charge with stripe
     *
     * @param orderId       - Order id
     * @param chargeRequest - JSON body
     * @return
     */

    public String chargeStripe(Integer orderId, ChargeRequest chargeRequest) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        if (!order.getCart_orderPayment().getPayment().equals(PAYMENT.Online_bank_payment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This is not payment method, you've selected!");
        }

        chargeRequest.setAmount(order.getTotalPrice().intValue() * 100);
        chargeRequest.setDescription("Order id: " + order.getId());
        chargeRequest.setCurrency(ChargeRequest.Currency.EUR);

        Charge charge;

        try {
            charge = stripeService.charge(chargeRequest);
        } catch (InvalidRequestException | APIConnectionException | CardException | APIException | AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while checking out!");
        }

        order.setOrderState(OrderState.PAID);
        order.setPaymentDetails("Charge id: " + charge.getId() + "; Status: " + charge.getStatus() + "; Time paid: " + new Date(TimeUnit.SECONDS.toMillis(charge.getCreated())));

        sendEmail(order, " paid successfully!", "Thanks for your purchase!<br>We will work as hard as we can, to deliver the order to you, as soon as possible!");
        orderRepository.save(order);
        return "Successfully paid!";
    }

    /**
     * Create paypal payment
     *
     * @param orderId - Order id
     * @return - payment id;
     */

    public String createPayment(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.header("prefer", "return=representation");
        request.requestBody(createBuildRequestBody(order));

        HttpResponse<com.paypal.orders.Order> response;

        try {
            response = payPalClient.client().execute(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return response.result().id();
    }

    /**
     * Capture paypal payment after it was created
     *
     * @param orderId - order id
     * @param paypalId - paypal-payment id
     * @return payment id;
     */

    public String capturePayment(Integer orderId, String paypalId) {
        OrdersCaptureRequest request = new OrdersCaptureRequest(paypalId);
        request.requestBody(captureBuildRequestBody());

        HttpResponse<com.paypal.orders.Order> response;

        try {
            response = payPalClient.client().execute(request);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        for (PurchaseUnit purchaseUnit : response.result().purchaseUnits()) {
            purchaseUnit.amountWithBreakdown();
            for (Capture capture : purchaseUnit.payments().captures()) {
                if (capture.status().equals("COMPLETED")) {
                    Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
                    order.setOrderState(OrderState.PAID);
                    order.setPaymentDetails("Charge id: " + capture.id() + "; Status: " + capture.status() + "; Time paid: " + capture.createTime() + " GMT");

                    sendEmail(order, " paid successfully!", "Thanks for your purchase!<br>We will work as hard as we can, to deliver the order to you, as soon as possible!");
                    orderRepository.save(order);
                }
            }
        }

        return response.result().id();
    }

    public OrderDto findOrderById(Integer id) {
        return new OrderDto(orderRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!")));
    }

    /**
     * Create Order
     *
     * @param order        - JSON body
     * @param token        - User's token
     * @param cartId       - Cart Id
     * @param shouldUpdate - If user is logged in and shouldUpdate is true, user's delivery informations will be updated
     * @return
     */

    public String createOrder(Order order, String token, String cartId, Boolean shouldUpdate) {
        token = jwtUtil.shortToken(token);

        Cart cart = null;
        User user = null;

        if (cartId != null) {
            if (cartRepository.existsById(cartId)) {
                cart = cartRepository.findById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
            }
        }

        if (token != null && jwtUtil.validateToken(token).equals("Valid")) {
            user = userRepository.findByEmail(jwtUtil.extractEmail(token));
        }

        cart = (user == null ? cart : user.getCart());

        if (user == null && userRepository.existsByEmail(order.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email account already exists, please login!");
        } else if (user != null && !user.getEmail().equals(order.getEmail()) && userRepository.existsByEmail(order.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email address is assigned to anouther user account!");
        }

        if (cart.getCart_orderPayment() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please set payment method!");
        }
        if (cart.getCart_orderDelivery() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Please set delivery method!");
        }

        proceedCreateOrder(order, cart, user);
        restoreCart(cart);
        sendEmail(order, " placed!", (order.getCart_orderPayment().getPayment().equals(PAYMENT.Paypal) || order.getCart_orderPayment().getPayment().equals(PAYMENT.Online_bank_payment) ? "Please pay with method you have selected, by clicking the button bellow!" : "We will work as hard as we can, to deliver the order to you, as soon as possible!"));

        if (user != null && shouldUpdate != null && shouldUpdate) {
            updateUsersDeliveryInformations(user, order);
        }

        return order.getId().toString();
    }

    public void proceedCreateOrder(Order order, Cart cart, User user) {
        Boolean isOnlinePayment = cart.getCart_orderPayment().getPayment().equals(PAYMENT.Paypal) || cart.getCart_orderPayment().getPayment().equals(PAYMENT.Online_bank_payment);

        order.setTotalPrice(totalPrice(cart));
        order.setCart_orderPayment(cart.getCart_orderPayment());
        order.setCart_orderDelivery(cart.getCart_orderDelivery());
        order.setCoupon(cart.getCoupon());
        order.setUser(user);

        order.setOrderState((isOnlinePayment) ? OrderState.AWAITING_PAYMENT : OrderState.PROCESSING);

        List<OrderItem> orderItems = addCartItemsToOrder(cart, order);
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
    }

    public List<OrderItem> addCartItemsToOrder(Cart cart, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        if (cart.getCartItems().size() == 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cart is empty!");
        }
        for (CartItem cartItem : cart.getCartItems()) {
            ProductSize productSize = productSizeRepository.findBySizeAndProductOption_Product_titleAndProductOption_UnderTitle(cartItem.getProductSize(), cartItem.getProductOption().getProduct().getTitle(), cartItem.getProductOption().getUnderTitle()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product size not found!"));
            if (productSize.getOnStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not enough of product in stock! (Product)- " + cartItem.getProductOption().getProduct().getTitle() + " - " + cartItem.getProductOption().getUnderTitle() + " Size: " + cartItem.getProductSize() + " In stock: " + productSize.getOnStock());
            }

            productSize.setOnStock(productSize.getOnStock() - cartItem.getQuantity());
            productSizeRepository.save(productSize);

            cartItem.getProductOption().setSoldTimes(cartItem.getProductOption().getSoldTimes() + cartItem.getQuantity());
            productOptionRepository.save(cartItem.getProductOption());

            orderItems.add(new OrderItem(cartItem.getQuantity(), (cartItem.getProductOption().getPrice().subtract(cartItem.getProductOption().getDiscount())).multiply(BigDecimal.valueOf(cartItem.getQuantity())), cartItem.getProductOption(), cartItem.getProductSize(), order));
        }

        return orderItems;
    }

    public void restoreCart(Cart cart) {
        cart.setCoupon(null);
        cart.removeAll();

        cart.getCart_orderDelivery().setCartId(null);
        cart.getCart_orderPayment().setCartId(null);

        cart_orderDeliveryRepository.save(cart.getCart_orderDelivery());
        cart_orderPaymentRepository.save(cart.getCart_orderPayment());

        cart.setCart_orderDelivery(null);
        cart.setCart_orderPayment(null);
        cartRepository.save(cart);

        List<CartItem> dl = cartItemRepository.findAllByCartId(cart.getId());
        cartItemRepository.deleteAll(dl);
    }

    public void sendEmail(Order order, String action, String text) {
        Map<String, Object> model = new HashMap<>();
        model.put("order", order);
        model.put("link", FRONT_END + "/checkout/" + order.getId());
        model.put("text", text);
        mailService.sendEmail(new SendEmailRequest(new String[]{order.getEmail()}, "Order id: " + order.getId() + action, null), model, "invoice.ftl");
    }

    public void updateUsersDeliveryInformations(User user, Order order) {
        user.setCity(order.getCity());
        user.setPostalCode(order.getPostalCode());
        user.setAddress(order.getAddress());
        user.setCountry(order.getCountry());
        userRepository.save(user);
    }

    public BigDecimal totalPrice(Cart cart) {
        BigDecimal totalPriceWithoutPaymentAndDelivery = cart.getCartItems().stream().reduce(BigDecimal.ZERO, (bd, item) -> bd.add((item.getProductOption().getPrice().subtract(item.getProductOption().getDiscount())).multiply(BigDecimal.valueOf(item.getQuantity()))), BigDecimal::add);
        if (cart.getCoupon() != null) {
            if (cart.getCoupon().getDiscountType().equals(DISCOUNT.TOTAL)) {
                totalPriceWithoutPaymentAndDelivery = totalPriceWithoutPaymentAndDelivery.subtract(cart.getCoupon().getDiscount());
            } else if (cart.getCoupon().getDiscountType().equals(DISCOUNT.PERCENTAGE)) {
                totalPriceWithoutPaymentAndDelivery = (totalPriceWithoutPaymentAndDelivery.divide(new BigDecimal(100))).multiply(new BigDecimal(100).subtract(cart.getCoupon().getDiscount()));
            }
        }
        return totalPriceWithoutPaymentAndDelivery.add(cart.getCart_orderPayment().getPrice()).add(cart.getCart_orderDelivery().getPrice());
    }

    /**
     * Cancel order
     *
     * @param token   - User's token
     * @param orderId - Order Id
     * @return
     */

    public String cancelOrder(String token, Integer orderId) {
        token = jwtUtil.shortToken(token);
        String email = (token != null ? jwtUtil.extractEmail(token) : null);

        Order order = orderRepository.findByIdAndUser_Email(orderId, email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));

        if (order.getOrderState() != OrderState.PROCESSING && order.getOrderState() != OrderState.AWAITING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order cannot be canceled!");
        }

        sendEmail(order, " canceled", "Your order was refunded!<br>We are sorry to hear that!<br>We hope to see you again soon!");
        order.setOrderState(OrderState.CANCELED);

        orderRepository.save(order);

        return "Order was canceled!";
    }

    public OrderRequest captureBuildRequestBody() {
        return new OrderRequest();
    }

    private OrderRequest createBuildRequestBody(Order order) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        ApplicationContext applicationContext = new ApplicationContext().brandName("E-Sneakers").landingPage("BILLING")
                .shippingPreference("SET_PROVIDED_ADDRESS");
        orderRequest.applicationContext(applicationContext);

        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<PurchaseUnitRequest>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest().referenceId("PUHF")
                .description(order.getNote()).customId(order.getId().toString()).softDescriptor("E-Sneakers")
                .amountWithBreakdown(new AmountWithBreakdown().currencyCode("EUR").value(order.getTotalPrice().toString())
                        .amountBreakdown(new AmountBreakdown().itemTotal(new Money().currencyCode("EUR").value(order.getTotalPrice().subtract(order.getCart_orderDelivery().getPrice()).subtract(order.getCart_orderPayment().getPrice()).subtract(order.getTotalPrice().divide(new BigDecimal(1.2), new MathContext(2, RoundingMode.HALF_UP))).toString()))
                                .shipping(new Money().currencyCode("EUR").value(order.getCart_orderDelivery().getPrice().add(order.getCart_orderPayment().getPrice()).toString()))
                                .taxTotal(new Money().currencyCode("EUR").value(order.getTotalPrice().divide(new BigDecimal(1.2), new MathContext(2, RoundingMode.HALF_UP)).toString()))))
                .shippingDetail(new ShippingDetail().name(new Name().fullName(order.getName()))
                        .addressPortable(new AddressPortable().addressLine1(order.getAddress())
                                .adminArea2(order.getCountry()).adminArea1(order.getCity()).postalCode(order.getPostalCode()).countryCode("SK")));

        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);
        return orderRequest;
    }

}
