package com.projekt.projekt.Controllers;

import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Requests.*;
import com.projekt.projekt.Services.BasicsService;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class BasicsController {

    private BasicsService basicsService;

    public BasicsController(BasicsService basicsService) {
        this.basicsService=basicsService;
    }

    @PostMapping("/authenticate")
    public String generateToken(@RequestBody AuthRequest authRequest) {
        return basicsService.auth(authRequest);
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String code, HttpServletResponse httpResponse) {
        return basicsService.registrationConfirm(code, httpResponse);
    }

    @GetMapping("/change-email")
    public String changeEmail(@RequestParam String code, HttpServletResponse httpResponse) {
        return basicsService.changeEmailConfirm(code, httpResponse);
    }

    @GetMapping("/change-password")
    public String changePassword(@RequestParam String code, HttpServletResponse httpServletResponse) {
        return basicsService.changePasswordConfirm(code, httpServletResponse);
    }

    @PostMapping("/change-password-request")
    public String changePassword(@RequestBody PasswordChangeRequest email) {
        return basicsService.sendChangePasswordLink(email.getEmail());
    }

    @GetMapping("/user")
    public User retrieveUserInfo(@RequestHeader(value = "token") String token) {
        return basicsService.retrieveUser(token);
    }

    @PutMapping("/update-informations")
    public String updateUser(@RequestHeader(value = "token") String token, @Valid @RequestBody User requestForm, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }

        return basicsService.updateUser(token, requestForm);
    }

    @PutMapping("/update-password")
    public String updatePassword(@RequestHeader(value = "token") String token, @Valid @RequestBody ChangePasswordRequest changePasswordRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }

        return basicsService.updatePassword(token, changePasswordRequest);
    }

    @PutMapping("/2FA")
    public String update2FA(@RequestHeader(value = "token") String token, @RequestBody TwoPhCodeRequest code) {

        return basicsService.turnOnOff2FA(token, code.getTwoFactorCode());
    }

    @PostMapping("/image")
    public String uploadImage(@RequestHeader(value = "token") String token, @RequestParam("file") MultipartFile image) {
        return basicsService.uploadImage(image, token);
    }

    @GetMapping(value = "/image/{image}")
    public void userImage(HttpServletResponse response, @PathVariable String image) {
        Path path = Paths.get("src\\main\\resources\\static\\UsersImages\\" + image);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        try {
            InputStream is = Files.newInputStream(path);
            IOUtils.copy(is, response.getOutputStream());
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Error while streaming image!");
        }
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest registerRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, result.getFieldError().getDefaultMessage());
        }
        return basicsService.registerUser(registerRequest);
    }

    @GetMapping("/orders")
    public Page<Order> getUsersOrders(@RequestHeader(value = "token") String token, @RequestParam Integer page, @RequestParam Integer size) {

        return basicsService.findAllByUsersEmail(token, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
    }
}
