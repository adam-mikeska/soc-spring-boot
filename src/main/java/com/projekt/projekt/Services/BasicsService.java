package com.projekt.projekt.Services;

import com.projekt.projekt.Models.Ecommerce.Cart;
import com.projekt.projekt.Models.Ecommerce.Order;
import com.projekt.projekt.Models.security.ChangeEmail;
import com.projekt.projekt.Models.security.ChangePassword;
import com.projekt.projekt.Models.security.RegistrationToken;
import com.projekt.projekt.Models.User;
import com.projekt.projekt.Models.security.TwoPhCode;
import com.projekt.projekt.Repositories.Ecommerce.CartRepository;
import com.projekt.projekt.Repositories.Ecommerce.OrderRepository;
import com.projekt.projekt.Repositories.RoleRepository;
import com.projekt.projekt.Repositories.security.ChangeEmailRepository;
import com.projekt.projekt.Repositories.security.ChangePasswordRepository;
import com.projekt.projekt.Repositories.security.RegistrationTokenRepository;
import com.projekt.projekt.Repositories.UserRepository;
import com.projekt.projekt.Repositories.security.TwoPhCodeRepository;
import com.projekt.projekt.Requests.*;
import com.projekt.projekt.Security.JwtUtil;

import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class BasicsService {

    final private UserRepository userRepository;
    final private OrderRepository orderRepository;
    final private PasswordEncoder passwordEncoder;
    final private RegistrationTokenRepository registrationTokenRepository;
    final private ChangeEmailRepository changeEmailRepository;
    final private JwtUtil jwtUtil;
    final private RoleRepository roleRepository;
    final private TwoPhCodeRepository twoPhCodeRepository;
    final private ChangePasswordRepository changePasswordRepository;
    final private CartRepository cartRepository;
    final private AuthenticationManager authenticationManager;
    final private MailService mailService;
    @Value("${frontend}")
    private String FRONT_END;
    @Value("${backend}")
    private String BACK_END;

    public BasicsService(UserRepository userRepository, PasswordEncoder passwordEncoder, RegistrationTokenRepository registrationTokenRepository, ChangeEmailRepository changeEmailRepository, JwtUtil jwtUtil, RoleRepository roleRepository, OrderRepository orderRepository, JavaMailSender emailSender, TwoPhCodeRepository twoPhCodeRepository, ChangePasswordRepository changePasswordRepository, CartRepository cartRepository, AuthenticationManager authenticationManager, Configuration configuration, MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationTokenRepository = registrationTokenRepository;
        this.changeEmailRepository = changeEmailRepository;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.twoPhCodeRepository = twoPhCodeRepository;
        this.changePasswordRepository = changePasswordRepository;
        this.cartRepository = cartRepository;
        this.authenticationManager = authenticationManager;
        this.mailService = mailService;
    }

    public User retrieveUser(String token) {
        token = jwtUtil.shortToken(token);
        if (userRepository.existsByEmail(jwtUtil.extractEmail(token))) {
            return userRepository.findByEmail(jwtUtil.extractEmail(token));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error occured!");
        }
    }

    /**
     * Update user
     * @param token - User's token
     * @param requestForm - JSON body
     * @return
     */

    public String updateUser(String token, User requestForm) {
        token = jwtUtil.shortToken(token);

        User user = userRepository.findByEmail(jwtUtil.extractEmail(token));

        if (user.equals(requestForm)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You haven't updated anything!");
        }

        if (userRepository.existsByEmail(requestForm.getEmail()) && !user.getEmail().equals(requestForm.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email address already exists!");
        }
        if (requestForm.getTelNumber() != null && userRepository.existsByTelNumber(requestForm.getTelNumber()) && !requestForm.getTelNumber().equals(user.getTelNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This mobile number is already assigned to one of our users.");
        }
        if (!userRepository.existsByEmail(requestForm.getEmail()) && !user.getEmail().equals(requestForm.getEmail())) {
            proceedUpdateUser(user, requestForm);
            sendChangeEmailLink(user, requestForm);
            return "Logout";
        }

        proceedUpdateUser(user, requestForm);
        return "Successfully updated!";
    }

    public void proceedUpdateUser(User memberFromDb, User requestForm) {
        memberFromDb.setValues(requestForm, false);
        userRepository.save(memberFromDb);
    }

    /**
     * Update password
     * @param token - User's token
     * @param requestForm - JSON body
     * @return
     */

    public String updatePassword(String token, ChangePasswordRequest requestForm) {
        token = jwtUtil.shortToken(token);
        User user = userRepository.findByEmail(jwtUtil.extractEmail(token));

        if (!passwordEncoder.matches(requestForm.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Bad current password!");
        }
        if (!requestForm.getNewPassword().equals(requestForm.getRepeatNewPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Confirmation password is not equal to new password!");
        }
        if (passwordEncoder.matches(requestForm.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "New password is equal to your current one!");
        }

        User memberFromDb = userRepository.findById(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found!"));
        memberFromDb.setPassword(passwordEncoder.encode(requestForm.getNewPassword()));

        userRepository.save(memberFromDb);
        return "Successfully updated!";
    }

    /**
     * Upload image
     * @param multipartFile - New Image
     * @param token - User's token
     * @return
     */

    public String uploadImage(MultipartFile multipartFile, String token) {
        token = jwtUtil.shortToken(token);
        User user = userRepository.findByEmail(jwtUtil.extractEmail(token));

        List<String> contentTypes = Arrays.asList("image/png", "image/jpeg", "image/gif");

        if (multipartFile.getSize() > 3000000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum file size exceeded!");
        }

        if (!contentTypes.contains( multipartFile.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Please upload only png, jpg, gif pictures.");
        }

        String[] parts = multipartFile.getOriginalFilename().split("\\.");
        String imageName = user.getId() + "." + parts[1];

        if (!user.getImage().equals("img_avatar_female.png") && !user.getImage().equals("img_avatar_male.png")) {
            new File("src\\main\\resources\\static\\UsersImages\\" + user.getImage()).delete();
        }

        try {
            Files.copy(multipartFile.getInputStream(), Paths.get("src\\main\\resources\\static\\UsersImages\\" + imageName));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while uploading image!");
        }

        user.setImage(imageName);
        userRepository.save(user);

        return "File uploaded successfully!";
    }

    /**
     * Turn ON or OFF 2FA Code
     * @param token - User's token
     * @param code - New Code
     * @return
     */

    public String turnOnOff2FA(String token, String code) {
        token = jwtUtil.shortToken(token);

        User user = null;
        TwoPhCode twoPhCode = null;

        if (token != null) {
            user = userRepository.findByEmail(jwtUtil.extractEmail(token));
            twoPhCode = twoPhCodeRepository.findByEmail(jwtUtil.extractEmail(token));
        }

        if (code == null && twoPhCode == null) {
            generate2FACode(user, "Confirmation code");
            return "Code was sent to your email address!";
        } else if (twoPhCode != null && twoPhCode.getTwoPhCode().equals(code)) {
            user.setTwoPhVerEnabled(!user.getTwoPhVerEnabled());
            userRepository.save(user);
            twoPhCodeRepository.delete(twoPhCode);
            return "Sucessfully updated 2FA.";
        } else if (twoPhCode != null && code == null) {
            return "Code already generated!";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad confirmation code!");
        }
    }

    /**
     * Send change password link to email
     * @param email - User's email
     * @return
     */

    public String sendChangePasswordLink(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email does not exist.");
        }
        User user = userRepository.findByEmail(email);

        if (!changePasswordRepository.existsByEmail(email)) {
            ChangePassword changePassword = new ChangePassword(UUID.randomUUID().toString().toUpperCase().split("-")[0], email);
            changePasswordRepository.save(changePassword);
        }
        ChangePassword changePassword = changePasswordRepository.findByEmail(user.getEmail());

        Map<String, Object> model = new HashMap<>();
        model.put("link", BACK_END + "/api/change-password?code=" + changePassword.getConfirmationToken());
        model.put("name", user.getName());
        model.put("newPassword", changePassword.getCurPassword());

        mailService.sendEmail(new SendEmailRequest(new String[]{user.getEmail()}, "Password change!", null), model, "reset-password.ftl");
        checkForExpirationChangePassword(changePassword);

        return "Confirmation email has been sent to your email address";
    }

    /**
     * Send confirmation email to change password
     * @param code - Confirmation code
     * @param httpResponse - Response to redirect user
     * @return
     */

    public String changePasswordConfirm(String code, HttpServletResponse httpResponse) {
        if (changePasswordRepository.existsByConfirmationToken(code)) {
            ChangePassword changePassword = changePasswordRepository.getChangePasswordByConfirmationToken(code);
            User user = userRepository.findByEmail(changePassword.getEmail());
            user.setPassword(passwordEncoder.encode(changePassword.getCurPassword()));

            userRepository.save(user);
            changePasswordRepository.delete(changePassword);

            redirect(httpResponse,"/login?scschngpassword=true");
            return "Successfully changed password";
        } else {
            redirect(httpResponse,"/login?scschngpassword=false");
            return "Error while changing password";
        }
    }

    /**
     * Register user
     * @param registerRequest - JSON body
     * @return
     */

    public String registerUser(RegisterRequest registerRequest) {
        if (!userRepository.existsByEmail(registerRequest.getEmail()) && !changeEmailRepository.existsByNewEmail(registerRequest.getEmail())) {
            User user = new User(registerRequest.getName(), registerRequest.getEmail(), roleRepository.findByName("USER").orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found!")), passwordEncoder.encode(registerRequest.getPassword()), registerRequest.getGender(), null, null);
            user.setEnabled(false);
            userRepository.save(user);

            RegistrationToken registrationToken = new RegistrationToken(user.getEmail());
            registrationTokenRepository.save(registrationToken);

            sendRegistrationLink(user, registrationToken);

            return "Please confirm your email!";
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email address already exists!");
        }
    }

    public String sendRegistrationLink(User user, RegistrationToken registrationToken) {
        Map<String, Object> model = new HashMap<>();
        model.put("link", BACK_END + "/api/verify-email?code=" + registrationToken.getConfirmationToken());
        model.put("name", user.getName());
        model.put("text", "Please confirm your email");
        model.put("text1", "Please validate your email address in order to start shopping!");

        mailService.sendEmail(new SendEmailRequest(new String[]{user.getEmail()}, "Please confirm your registration!", null), model, "confirm-email.ftl");

        checkForExpirationRegCode(registrationToken, user);
        return "Confirmation email has been sent to your email address";
    }

    /**
     * Confirm registration
     * @param code - Confirmation code
     * @param httpResponse - Response to redirect user
     * @return
     */

    public String registrationConfirm(String code, HttpServletResponse httpResponse) {
        if (registrationTokenRepository.existsByConfirmationToken(code)) {
            RegistrationToken registrationToken = registrationTokenRepository.getRegistrationTokenByConfirmationToken(code);

            User user = userRepository.findByEmail(registrationToken.getEmail());
            user.setEnabled(true);
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);

            user.setCart(cart);
            userRepository.save(user);
            registrationTokenRepository.delete(registrationToken);

            redirect(httpResponse,"/login?scsrg=true");
            return "Sucessfully registered";
        } else {
            redirect(httpResponse,"/login?scsrg=false");
            return "Error";
        }
    }

    /**
     * Send email to confirm email change
     * @param user - User
     * @param requestForm - JSON body
     * @return
     */

    public String sendChangeEmailLink(User user, User requestForm) {
        ChangeEmail changeEmail = new ChangeEmail(user.getEmail(), requestForm.getEmail());

        Map<String, Object> model = new HashMap<>();
        model.put("link", BACK_END + "/api/change-email?code=" + changeEmail.getConfirmationToken());
        model.put("name", user.getName());
        model.put("text", "Please confirm email change");
        model.put("text1", "If you didnt make  email change, please confirm this action by clicking on the button bellow!<br>New email will be: " + changeEmail.getNewEmail());

        mailService.sendEmail(new SendEmailRequest(new String[]{user.getEmail()}, "Please confirm your email change!", null), model, "confirm-email.ftl");
        checkForExpirationChangeEmail(changeEmail);

        changeEmailRepository.save(changeEmail);
        return "Confirmation email has been sent to your email address";
    }

    /**
     * Confirm email change
     * @param code - Confirmation code
     * @param httpResponse - Response to redirect user
     * @return
     */

    public String changeEmailConfirm(String code, HttpServletResponse httpResponse) {
        if (changeEmailRepository.existsByConfirmationToken(code)) {

            ChangeEmail changeEmail = changeEmailRepository.getChangeEmailByConfirmationToken(code);
            User user = userRepository.findByEmail(changeEmail.getCurrentEmail());
            user.setEmail(changeEmail.getNewEmail());

            userRepository.save(user);
            changeEmailRepository.deleteAll(changeEmailRepository.findAllByCurrentEmail(changeEmail.getCurrentEmail()));

            redirect(httpResponse,"/login?scschngmail=true");
            return "Sucessfully changed email";
        } else {
            redirect(httpResponse,"/login?scschngmail=false");
            return "This email address is taken!";
        }
    }

    /**
     * Authentication
     * @param authRequest - JSON body
     * @return
     */

    public String auth(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail());
        TwoPhCode twoPhCode = twoPhCodeRepository.findByEmail(authRequest.getEmail());

        if (userRepository.existsByEmail(authRequest.getEmail()) && !user.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please confirm your account!");
        }
        if (userRepository.existsByEmail(authRequest.getEmail()) && !user.getNonLocked()) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Your account is locked till: " + user.getLockedTill());
        }
        if (changeEmailRepository.existsByNewEmail(authRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please confirm email change first!");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (Exception io) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad credentials!");
        }

        return authenticationPossibilities(user, authRequest, twoPhCode);
    }

    public String authenticationPossibilities(User user, AuthRequest authRequest, TwoPhCode twoPhCode) {
        if (user.getTwoPhVerEnabled() && twoPhCode == null) {
            generate2FACode(user, "Authentification code");
            return "Code has been generated!";
        } else if (user.getTwoPhVerEnabled() && twoPhCode != null && authRequest.getTwoPhCode() == null) {
            return "Already generated!";
        } else if (user.getTwoPhVerEnabled() && twoPhCode != null && twoPhCode.getTwoPhCode().equals(authRequest.getTwoPhCode())) {
            twoPhCodeRepository.delete(twoPhCode);
            return jwtUtil.generateToken(authRequest.getEmail());
        } else if (!user.getTwoPhVerEnabled()) {
            return jwtUtil.generateToken(authRequest.getEmail());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad auth code!");
        }
    }

    public void generate2FACode(User user, String text) {
        TwoPhCode twoPhCode = new TwoPhCode(UUID.randomUUID().toString().toUpperCase().substring(0, 5), user.getEmail());
        twoPhCodeRepository.save(twoPhCode);

        Map<String, Object> model = new HashMap<>();
        model.put("name", user.getName());
        model.put("text", text);
        model.put("code", twoPhCode.getTwoPhCode());

        mailService.sendEmail(new SendEmailRequest(new String[]{user.getEmail()}, text, null), model, "two-ph-code.ftl");
        checkForExpiration2FA(user);
    }

    public User checkForExpiration2FA(User user) {
        TwoPhCode twoPhCode = twoPhCodeRepository.findByEmail(user.getEmail());
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (twoPhCodeRepository.existsByEmail(user.getEmail())) {
                    twoPhCodeRepository.delete(twoPhCode);
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, 1, TimeUnit.MINUTES);
        return user;
    }

    public void checkForExpirationRegCode(RegistrationToken registrationToken, User user) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (registrationTokenRepository.existsByConfirmationToken(registrationToken.getConfirmationToken())) {
                    userRepository.delete(user);
                    registrationTokenRepository.delete(registrationToken);
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, 10, TimeUnit.MINUTES);
    }

    public void checkForExpirationChangeEmail(ChangeEmail changeEmail) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (changeEmailRepository.existsByConfirmationToken(changeEmail.getConfirmationToken())) {
                    changeEmailRepository.delete(changeEmail);
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, 10, TimeUnit.MINUTES);
    }

    public void checkForExpirationChangePassword(ChangePassword changePassword) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (changePasswordRepository.existsByConfirmationToken(changePassword.getConfirmationToken())) {
                    changePasswordRepository.delete(changePasswordRepository.findByEmail(changePassword.getEmail()));
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        executor.schedule(task, 10, TimeUnit.MINUTES);
    }

    public Page<Order> findAllByUsersEmail(String token, Pageable pageable) {
        token = jwtUtil.shortToken(token);

        return orderRepository.findAllByUser_Email(jwtUtil.extractEmail(token), pageable);
    }

    public void redirect(HttpServletResponse response,String link){
        try {
            response.sendRedirect(FRONT_END + link);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while redirecting!");
        }
    }


}
