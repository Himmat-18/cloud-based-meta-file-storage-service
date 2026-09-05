package org.example.cloud_storage.controller;

import org.example.cloud_storage.User;
import org.example.cloud_storage.dto.LoginResponse;
import org.example.cloud_storage.security.JwtService;
import org.example.cloud_storage.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService,
                          JwtService jwtService) {

        this.userService = userService;
        this.jwtService = jwtService;
    }

    // REGISTER USER
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {

        return userService.registerUser(user);
    }

    // LOGIN USER
    @PostMapping("/login")
    public LoginResponse loginUser(
            @RequestParam String email,
            @RequestParam String password) {

        User user = userService.loginUser(
                email,
                password
        );

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    // RESET PASSWORD
    @PutMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword) {

        userService.resetPassword(
                email,
                newPassword
        );

        return "Password reset successfully";
    }
}