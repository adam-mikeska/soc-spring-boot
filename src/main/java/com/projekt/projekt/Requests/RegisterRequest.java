package com.projekt.projekt.Requests;

import com.projekt.projekt.Enums.GENDER;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Name cant be empty")
    private String name;
    @Column(unique = true)
    @Pattern(regexp = "^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", message = "Please enter valid email address!")
    @Email
    @NotBlank(message = "Email cant be empty")
    @Length(max = 100, message = "Maximum size exceeded!")
    private String email;
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Gender cant be empty")
    private GENDER gender;
    @NotBlank(message = "Please enter password")
    @Length(min = 8, max = 255, message = "Please enter password between 8 and 255 characters.")
    private String password;

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

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
