package com.projekt.projekt.Models.Ecommerce;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
public class Alert {
    @Id
    @Column(unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Please set some text")
    private String text;
    @NotBlank(message = "Please set color")
    private String color;

    public Alert() {
    }

    public Alert(String text,String color) {
        this.text = text;
        this.color=color;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alert)) return false;
        Alert alert = (Alert) o;
        return text.equals(alert.text) &&
                color.equals(alert.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, color);
    }
}
