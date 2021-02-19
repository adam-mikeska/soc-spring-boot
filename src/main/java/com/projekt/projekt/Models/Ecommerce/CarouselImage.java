package com.projekt.projekt.Models.Ecommerce;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
public class CarouselImage {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Please set some text")
    private String text;
    @NotBlank(message = "Please set link")
    private String link;
    @NotBlank(message = "Please set image")
    private String image;

    public CarouselImage() {
    }

    public CarouselImage(String text, String link, String image) {
        this.text = text;
        this.link = link;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarouselImage)) return false;
        CarouselImage carousel = (CarouselImage) o;
        return text.equals(carousel.text) && link.equals(carousel.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, link, image);
    }
}
