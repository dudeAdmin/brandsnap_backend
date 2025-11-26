package com.brandsnap.service;

import com.brandsnap.model.User;
import com.brandsnap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> loginUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findOrCreateGoogleUser(String email, String payload) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Extract name from payload
        String name = extractNameFromPayload(payload);
        if (name == null || name.isEmpty()) {
            name = email.split("@")[0]; // Use email prefix as fallback
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(name);
        newUser.setProvider(User.AuthProvider.GOOGLE);
        newUser.setProviderId(extractSubFromPayload(payload));
        newUser.setPassword(null); // No password for OAuth2 users

        return userRepository.save(newUser);
    }

    private String extractNameFromPayload(String payload) {
        try {
            int nameStart = payload.indexOf("\"name\":\"") + 8;
            if (nameStart == 7)
                return null;
            int nameEnd = payload.indexOf("\"", nameStart);
            return payload.substring(nameStart, nameEnd);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractSubFromPayload(String payload) {
        try {
            int subStart = payload.indexOf("\"sub\":\"") + 7;
            if (subStart == 6)
                return null;
            int subEnd = payload.indexOf("\"", subStart);
            return payload.substring(subStart, subEnd);
        } catch (Exception e) {
            return null;
        }
    }
}
