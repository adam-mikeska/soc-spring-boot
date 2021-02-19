package com.projekt.projekt.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageCategory {
    private Integer parrentId;
    @NotBlank(message = "Name cannot be blank!")
    private String name;

    public Integer getParrentId() {
        return parrentId;
    }

    public void setParrentId(Integer parrentId) {
        this.parrentId = parrentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
