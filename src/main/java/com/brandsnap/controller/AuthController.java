package com.brandsnap.controller;

import com.brandsnap.model.User;
import com.brandsnap.payload.response.JwtResponse;
import com.brandsnap.security.JwtUtils;
import com.brandsnap.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // Get user ID from database (optional, if needed in response)
            // For now we'll use a placeholder or fetch it if UserDetails has it
            // Assuming CustomUserDetails implements UserDetails and has getId()
            // If not, we might need to fetch user again or cast if possible.
            // For simplicity, let's fetch the user to get the ID.
            User user = userService.loginUser(username, password).orElseThrow();

            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    userDetails.getUsername(),
                    user.getEmail(),
                    roles));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody Map<String, String> request) {
        String credential = request.get("credential");

        try {
            // In a real implementation, you would verify the Google JWT token here
            // For now, we'll decode it to get the email
            // You should use Google's token verification library in production

            String[] parts = credential.split("\\.");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid credential format"));
            }

            // Decode the payload (base64url)
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            // Parse JSON to get email (simplified - use a JSON library in production)
            String email = extractEmailFromPayload(payload);

            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Could not extract email from credential"));
            }

            // Find or create user
            User user = userService.findOrCreateGoogleUser(email, payload);

            // Generate JWT token
            String jwt = jwtUtils.generateTokenFromEmail(email);

            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    List.of("ROLE_USER")));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Google authentication failed: " + e.getMessage()));
        }
    }

    private String extractEmailFromPayload(String payload) {
        try {
            // Simple JSON parsing - in production use Jackson or Gson
            int emailStart = payload.indexOf("\"email\":\"") + 9;
            if (emailStart == 8)
                return null;
            int emailEnd = payload.indexOf("\"", emailStart);
            return payload.substring(emailStart, emailEnd);
        } catch (Exception e) {
            return null;
        }
    }
}
