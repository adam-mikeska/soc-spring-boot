package com.projekt.projekt.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @NotBlank(message = "Please enter email")
    private String email;
    @Size(min = 6)
    private String password;
    @Size(min = 5)
    private String twoPhCode;

    public String getTwoPhCode() {
        return twoPhCode;
    }

    public void setTwoPhCode(String twoPhCode) {
        this.twoPhCode = twoPhCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
