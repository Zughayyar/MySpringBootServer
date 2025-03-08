package com.anascompany.server.services;


import com.anascompany.server.models.LoginUser;
import com.anascompany.server.models.User;
import com.anascompany.server.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a User
    public User registerUser(User newUser, BindingResult bindingResult) {
        Optional<User> existingUser = userRepository.findByEmail(newUser.getEmail());

        // Check if email is already taken
        if (existingUser.isPresent()) {
            bindingResult.rejectValue("email", "email.exists", "This email already exists");
        }

        // Check if passwords match
        if (!newUser.getPassword().equals(newUser.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.not.match", "Password does not match");
        }

        // Return null if still errors
        if (bindingResult.hasErrors()) {
            return null;
        }

        // Hash and set password, save user to database
        String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newUser.setPassword(hashedPassword);
        User savedUser = userRepository.save(newUser);

        // making sure object returned to controller is without visible password.
        savedUser.setConfirmPassword("");
        return userRepository.save(savedUser);
    }

    // Login User:
    public User loginUser(LoginUser newLoginUser, BindingResult bindingResult) {

        // Find user in the DB by email
        Optional<User> potentialUser = userRepository.findByEmail(newLoginUser.getEmail());
        if (potentialUser.isEmpty()) {
            bindingResult.rejectValue("email", "NotFound", "Email not found!");
            return null;
        }

        // Get the user object
        User user = potentialUser.get();

        // Reject if BCrypt password match fails
        if (!BCrypt.checkpw(newLoginUser.getPassword(), user.getPassword())) {
            bindingResult.rejectValue("password", "Invalid", "Invalid Password!");
        }

        // Return null if result has errors
        if (bindingResult.hasErrors()) {
            return null;
        } else {
            return user;
        }
    }




}
