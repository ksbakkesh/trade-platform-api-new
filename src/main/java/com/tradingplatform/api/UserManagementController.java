package com.tradingplatform.api;

import com.tradingplatform.domain.User;
import com.tradingplatform.domain.enums.UserRole;
import java.time.Instant;
import com.tradingplatform.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserManagementController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                         @RequestBody UpdateRequest req) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (req.username() != null && !req.username().isBlank()) user.setUsername(req.username());
        if (req.email() != null && !req.email().isBlank()) user.setEmail(req.email());
        if (req.password() != null && !req.password().isBlank()) user.setPasswordHash(passwordEncoder.encode(req.password()));
        if (req.role() != null && !req.role().isBlank()) {
            try { UserRole r = UserRole.valueOf(req.role()); if (r != UserRole.ADMIN) user.setRole(r); } catch (Exception ignored) {}
        }
        userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    public record UserResponse(Long id, String username, String email, String role, boolean active, Instant createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getEmail(),
                    u.getRole().name(), u.isActive(), u.getCreatedAt());
        }
    }

    public record UpdateRequest(String username, String email, String password, String role) {}
}