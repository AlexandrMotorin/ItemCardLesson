package com.example.englishwordsapp.controller;

import com.example.englishwordsapp.dto.LoginRequest;
import com.example.englishwordsapp.dto.RegisterRequest;
import com.example.englishwordsapp.repository.RoleRepository;
import com.example.englishwordsapp.repository.UserRepository;
import com.example.englishwordsapp.security.JwtService;
import com.example.englishwordsapp.security.Role;
import com.example.englishwordsapp.security.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // --- REST API endpoints (JWT) ---

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);

        return ResponseEntity.ok(Map.of("token", jwt, "type", "Bearer"));
    }

    // --- Thymeleaf endpoints ---

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                               BindingResult bindingResult,
                               Model model) {
        // Check validation errors from annotations
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", registerRequest.getUsername());
            model.addAttribute("email", registerRequest.getEmail());
            return "register";
        }

        // Validate passwords match (can't be done via annotation)
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("username", registerRequest.getUsername());
            model.addAttribute("email", registerRequest.getEmail());
            return "register";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            model.addAttribute("error", "Username is already taken.");
            model.addAttribute("username", registerRequest.getUsername());
            model.addAttribute("email", registerRequest.getEmail());
            return "register";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            model.addAttribute("error", "Email is already registered.");
            model.addAttribute("username", registerRequest.getUsername());
            model.addAttribute("email", registerRequest.getEmail());
            return "register";
        }

        // Create and save the user with default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(roles);
        userRepository.save(user);

        return "redirect:/login?registered";
    }
}
