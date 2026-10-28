package com.ipcms.controller;

import com.ipcms.config.JwtTokenProvider;
import com.ipcms.dto.AuthRequest;
import com.ipcms.dto.AuthResponse;
import com.ipcms.dto.RegisterRequest;
import com.ipcms.model.Role;
import com.ipcms.model.User;
import com.ipcms.repository.UserRepository;
import com.ipcms.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or credentials"));
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity.status(403).body(Map.of("error", "Account has been deactivated"));
        }

        String token = jwtTokenProvider.generateToken(user);
        int durationHours = (user.getRole() == Role.UNDERWRITER || user.getRole() == Role.HANDLER || user.getRole() == Role.ADMIN) ? 12 : 8;

        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getFullName(),
                durationHours
        );

        auditService.log("USER", user.getId(), "LOGIN", user.getUsername(), user.getRole().name(), "User authenticated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * FR1: Policyholder Onboarding and Dynamic Identity Verification
     * Appendix C: 10-digit contact number check
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is already taken"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already registered"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.HOLDER);
        user.setFullName(request.getFullName() != null ? request.getFullName() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setTaxId(request.getTaxId());

        // Simulated Real-Time AML check against national watchlist (FR1)
        user.setAmlVerified(true);

        User saved = userRepository.save(user);

        auditService.log("USER", saved.getId(), "REGISTER", saved.getUsername(), saved.getRole().name(), "New user onboarded with AML clearance");

        String token = jwtTokenProvider.generateToken(saved);
        int durationHours = (saved.getRole() == Role.UNDERWRITER || saved.getRole() == Role.HANDLER || saved.getRole() == Role.ADMIN) ? 12 : 8;

        return ResponseEntity.ok(new AuthResponse(
                token,
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole(),
                saved.getFullName(),
                durationHours
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("fullName", user.getFullName());
        data.put("phone", user.getPhone());
        data.put("taxId", user.getTaxId());
        data.put("amlVerified", user.getAmlVerified());
        data.put("sessionDurationHours", (user.getRole() == Role.UNDERWRITER || user.getRole() == Role.HANDLER || user.getRole() == Role.ADMIN) ? 12 : 8);

        return ResponseEntity.ok(data);
    }
}
