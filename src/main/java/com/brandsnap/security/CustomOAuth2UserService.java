package com.brandsnap.security;

import com.brandsnap.model.User;
import com.brandsnap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oauth2User.getAttributes());

        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update user info if needed
            if (!user.getProvider().equals(User.AuthProvider.GOOGLE)) {
                throw new OAuth2AuthenticationException("Email already registered with " + user.getProvider());
            }
            user.setUsername(userInfo.getName());
        } else {
            // Create new user
            user = new User();
            user.setUsername(userInfo.getName());
            user.setEmail(userInfo.getEmail());
            user.setProvider(User.AuthProvider.GOOGLE);
            user.setProviderId(userInfo.getProviderId());
            user.setPassword(null); // No password for OAuth2 users
            user = userRepository.save(user);
        }

        return oauth2User;
    }
}
