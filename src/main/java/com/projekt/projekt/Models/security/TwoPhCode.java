package com.projekt.projekt.Models.security;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
public class TwoPhCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Size(min = 5)
    private String twoPhCode;
    private String email;

    public TwoPhCode() {
    }

    public TwoPhCode(String twoPhCode, String email) {
        this.twoPhCode = twoPhCode;
        this.email = email;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
}
