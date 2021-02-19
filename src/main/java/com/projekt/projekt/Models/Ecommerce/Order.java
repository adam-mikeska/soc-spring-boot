package com.projekt.projekt.Models.Ecommerce;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.projekt.projekt.Enums.DISCOUNT;
import com.projekt.projekt.Enums.OrderState;
import com.projekt.projekt.Models.User;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Order {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter name!")
    private String name;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter email!")
    @Email
    @Pattern(regexp = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", message = "Please enter valid email address!")
    private String email;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter address!")
    private String address;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter country!")
    private String country;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter city!")
    private String city;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter postal code!")
    private String postalCode;
    @Length(max = 100, message = "Maximum size exceeded!")
    @NotBlank(message = "Please enter tel number!")
    private String telNumber;
    @JsonFormat(pattern = "dd/MM/yy HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime created = LocalDateTime.now();
    @Length(max = 255, message = "Maximum size exceeded!")
    private String note;
    @OneToOne
    @JsonIgnore
    private User user;
    @Length(max = 2555)
    private String paymentDetails;
    @Enumerated(EnumType.STRING)
    private OrderState orderState;
    private BigDecimal totalPrice;
    @OneToOne
    private Cart_OrderDelivery cart_orderDelivery;
    @OneToOne
    private Cart_OrderPayment cart_orderPayment;
    @OneToOne
    private Coupon coupon;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public OrderState getOrderState() {
        return orderState;
    }

    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        if (this.coupon != null) {
            BigDecimal deliveryAndPaymentPrice = (this.cart_orderDelivery != null ? this.cart_orderDelivery.getPrice() : new BigDecimal(0)).add((this.cart_orderPayment != null ? this.cart_orderPayment.getPrice() : new BigDecimal(0)));

            BigDecimal totalPriceWithoutPaymentAndDelivery = totalPrice.subtract(deliveryAndPaymentPrice);

            if (this.coupon.getDiscountType().equals(DISCOUNT.PERCENTAGE)) {
                BigDecimal totalPriceDifference = ((totalPrice.subtract(this.totalPrice)).divide(new BigDecimal(100))).multiply(new BigDecimal(100).subtract(this.coupon.getDiscount()));
                totalPrice = this.totalPrice.add(totalPriceDifference);

                BigDecimal totalPriceWithoutCoupon =  totalPrice.subtract(deliveryAndPaymentPrice).divide(new BigDecimal(100).subtract(this.coupon.getDiscount())).multiply(new BigDecimal(100)).add(deliveryAndPaymentPrice);

                if (totalPriceWithoutCoupon.compareTo(this.coupon.getMinCartPrice()) == -1) {
                    totalPrice = totalPriceWithoutCoupon;
                    this.coupon=null;
                }
            } else if (this.coupon.getDiscountType().equals(DISCOUNT.TOTAL)) {
                if (totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getMinCartPrice()) == -1 || totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getDiscount()) != 1) {
                    totalPrice = totalPrice.add(this.coupon.getDiscount());
                    this.coupon = null;
                }
            }else if (this.coupon.getDiscountType().equals(DISCOUNT.DELIVERY)) {
                if (totalPriceWithoutPaymentAndDelivery.compareTo(this.coupon.getMinCartPrice()) == -1) {
                    this.getCart_orderDelivery().setPrice(this.cart_orderDelivery.getDeliveryENT().getPrice());
                    totalPrice = totalPrice.add(this.cart_orderDelivery.getDeliveryENT().getPrice());
                    this.coupon = null;
                }
            }
        }

        this.totalPrice = totalPrice;
    }

    public Cart_OrderDelivery getCart_orderDelivery() {
        return cart_orderDelivery;
    }

    public void setCart_orderDelivery(Cart_OrderDelivery cart_orderDelivery) {
        this.cart_orderDelivery = cart_orderDelivery;
    }

    public Cart_OrderPayment getCart_orderPayment() {
        return cart_orderPayment;
    }

    public void setCart_orderPayment(Cart_OrderPayment cart_orderPayment) {
        this.cart_orderPayment = cart_orderPayment;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }


    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return name.equals(order.name) &&
                email.equals(order.email) &&
                address.equals(order.address) &&
                country.equals(order.country) &&
                city.equals(order.city) &&
                postalCode.equals(order.postalCode) &&
                telNumber.equals(order.telNumber) &&
                orderState == order.orderState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, address, country, city, postalCode, telNumber, orderState);
    }


}
