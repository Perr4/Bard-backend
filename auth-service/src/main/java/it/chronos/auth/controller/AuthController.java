package it.chronos.auth.controller;

import it.chronos.auth.dto.AuthDtos.AuthResponse;
import it.chronos.auth.dto.AuthDtos.LoginRequest;
import it.chronos.auth.dto.AuthDtos.RegisterRequest;
import it.chronos.auth.dto.AuthDtos.UserProfile;
import it.chronos.auth.model.UserAccount;
import it.chronos.auth.model.UserRole;
import it.chronos.auth.kafka.UserRegisteredEventPublisher;
import it.chronos.auth.repository.UserAccountRepository;
import it.chronos.auth.repository.UserRoleRepository;
import it.chronos.auth.security.JwtService;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String DEFAULT_ROLE = "STUDENT";

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserRegisteredEventPublisher eventPublisher;

    public AuthController(
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            UserAccountRepository userRepository,
            UserRoleRepository roleRepository,
            JwtService jwtService,
            UserRegisteredEventPublisher eventPublisher) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        UserRole role = roleRepository.findByName(resolveRole(request.role()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not configured"));

        UserAccount user = new UserAccount();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));

        UserAccount savedUser = userRepository.save(user);
        //eventPublisher.publish(savedUser);
        return response(savedUser);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserAccount user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return response(user);
    }

    @GetMapping("/me")
    public UserProfile me(Principal principal) {
        UserAccount user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserProfile(user.getId(), user.getEmail(), user.getFullName(), roleNames(user));
    }

    private AuthResponse response(UserAccount user) {
        List<String> roles = roleNames(user);
        String token = jwtService.generateToken(user.getEmail(), roles.stream().map(role -> "ROLE_" + role).toList());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(), roles);
    }

    private List<String> roleNames(UserAccount user) {
        return user.getRoles().stream()
                .map(UserRole::getName)
                .sorted()
                .toList();
    }

    private String resolveRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return DEFAULT_ROLE;
        }
        return requestedRole.trim().toUpperCase();
    }
}
