package com.projekt.projekt.Models.Ecommerce;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
public class Brand {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Please enter brand name!")
    @Column(unique = true)
    @Length(max = 100, message = "Maximum size exceeded!")
    private String name;
    @NotBlank
    private String image;

    public Brand() {
    }

    public Brand(String name,String image) {
        this.name = name;
        this.image=image;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
