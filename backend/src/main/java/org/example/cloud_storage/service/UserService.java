package org.example.cloud_storage.service;

import org.example.cloud_storage.User;
import org.example.cloud_storage.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // REGISTER USER
    public User registerUser(User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    // LOGIN USER
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        String storedPassword = user.getPassword();

        // Old users may have a plain-text password
        if (!storedPassword.startsWith("$2a$")
                && !storedPassword.startsWith("$2b$")
                && !storedPassword.startsWith("$2y$")) {

            if (!storedPassword.equals(password)) {
                throw new RuntimeException("Invalid password");
            }

            // Convert old plain-text password to BCrypt
            user.setPassword(
                    passwordEncoder.encode(password)
            );

            userRepository.save(user);

            return user;
        }

        // Normal BCrypt password verification
        if (!passwordEncoder.matches(
                password,
                storedPassword)) {

            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    // RESET PASSWORD
    public void resetPassword(
            String email,
            String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        if (newPassword == null
                || newPassword.trim().isEmpty()) {

            throw new RuntimeException(
                    "New password cannot be empty"
            );
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);
    }
}