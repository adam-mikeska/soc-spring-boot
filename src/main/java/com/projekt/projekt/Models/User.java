package com.projekt.projekt.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.projekt.projekt.Enums.GENDER;
import com.projekt.projekt.Models.Ecommerce.Cart;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
public class User {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Name cannot be empty")
    @Length(max = 100, message = "Maximum size exceeded!")
    private String name;
    @Column(unique = true)
    @Pattern(regexp = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,5})+$", message = "Please enter valid email address!")
    @Email
    @NotBlank(message = "Email cant be empty")
    @Length(max = 100, message = "Maximum size exceeded!")
    private String email;
    @JsonIgnore
    private String password;
    @Length(max = 100, message = "Maximum size exceeded!")
    private String country;
    @Length(max = 100, message = "Maximum size exceeded!")
    private String postalCode;
    @Length(max = 100, message = "Maximum size exceeded!")
    private String address;
    @Length(max = 100, message = "Maximum size exceeded!")
    private String city;
    @Column(unique = true)
    @Size(max = 20, message = "Maximum size exceeded!")
    @Pattern(message = "Please enter valid tel number", regexp = "\\+(9[976]\\d|8[987530]\\d|6[987]\\d|5[90]\\d|42\\d|3[875]\\d|2[98654321]\\d|9[8543210]|8[6421]|6[6543210]|5[87654321]|4[987654310]|3[9643210]|2[70]|7|1)\\W*\\d\\W*\\d\\W*\\d\\W*\\d\\W*\\d\\W*\\d\\W*\\d\\W*\\d\\W*(\\d{1,2})$")
    private String telNumber;
    @JsonFormat( pattern = "dd/MM/yy HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime registrationDate = LocalDateTime.now();
    @Enumerated(EnumType.STRING)
    private GENDER gender;
    private String image;
    private String lockedTill;
    private Boolean twoPhVerEnabled = false;
    private Boolean isNonLocked = true;
    private Boolean isEnabled = true;
    @OneToOne
    private Cart cart;
    @OneToOne
    private Role role;

    public User() {
    }


    public User(String name, String email, Role role, String password, GENDER gender, String telNumber, Cart cart) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
        this.gender = gender;
        this.telNumber = telNumber;
        this.image = (this.gender == GENDER.Male ? "img_avatar_male.png" : "img_avatar_female.png");
        this.cart = cart;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public Boolean getTwoPhVerEnabled() {
        return twoPhVerEnabled;
    }

    public void setTwoPhVerEnabled(Boolean twoPhVerEnabled) {
        this.twoPhVerEnabled = twoPhVerEnabled;
    }

    public Boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getNonLocked() {
        return isNonLocked;
    }

    public void setNonLocked(Boolean nonLocked) {
        isNonLocked = nonLocked;
    }

    public String getLockedTill() {
        return lockedTill;
    }

    public void setLockedTill(String lockedTill) {
        this.lockedTill = lockedTill;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setValues(User user, Boolean isAdmin) {
        name = user.getName();
        if (isAdmin) {
            email = user.getEmail();
        }
        telNumber = user.getTelNumber();
        country = user.getCountry();
        address = user.getAddress();
        postalCode = user.getPostalCode();
        city = user.getCity();
        if (user.getGender() == GENDER.Male && !user.getGender().equals(gender) && image.equals("img_avatar_female.png")) {
            image = "img_avatar_male.png";
        }
        if (user.getGender() == GENDER.Female && !user.getGender().equals(gender) && image.equals("img_avatar_male.png")) {
            image = "img_avatar_female.png";
        }
        gender = user.getGender();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return name.equals(user.name) &&
                email.equals(user.email) &&
                Objects.equals(country, user.country) &&
                Objects.equals(telNumber, user.telNumber) &&
                Objects.equals(gender, user.gender) &&
                Objects.equals(postalCode, user.postalCode) &&
                Objects.equals(city, user.city) &&
                Objects.equals(address, user.address);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role, isNonLocked, password, country, telNumber, twoPhVerEnabled, isEnabled, registrationDate, gender, image);
    }
}
