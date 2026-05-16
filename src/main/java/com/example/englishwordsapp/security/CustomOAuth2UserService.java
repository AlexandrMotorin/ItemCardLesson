package com.example.englishwordsapp.security;

import com.example.englishwordsapp.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oauth2User.getAttribute("sub");
        if (providerId == null) {
            providerId = oauth2User.getName();
        }

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        if (name == null) {
            name = oauth2User.getAttribute("login");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            AuthProvider provider = parseProvider(registrationId);
            if (user.getProvider() != provider) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user = userRepository.save(user);
            }
        } else {
            user = new User();
            user.setUsername(email != null ? email : providerId);
            user.setEmail(email != null ? email : providerId + "@oauth2." + registrationId);
            user.setFullName(name);
            user.setProvider(parseProvider(registrationId));
            user.setProviderId(providerId);
            user.setPassword(null);
            user = userRepository.save(user);
        }

        return CustomUserDetails.build(user, oauth2User.getAttributes());
    }

    private AuthProvider parseProvider(String registrationId) {
        try {
            return AuthProvider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AuthProvider.LOCAL;
        }
    }
}
