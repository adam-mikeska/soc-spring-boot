package com.projekt.projekt.RemoveWhiteSpaces;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.support.WebBindingInitializer;

@ControllerAdvice
public class ModelInitializer implements WebBindingInitializer {

    @InitBinder
    @Override
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(String.class, new ModelEditor(true));
    }
}
