package br.com.wslima.javaspringbootrestapi.config.security.rest.controller;

import br.com.wslima.javaspringbootrestapi.config.security.JwtUtils;
import br.com.wslima.javaspringbootrestapi.commons.enums.ERole;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.RefreshToken;
import br.com.wslima.javaspringbootrestapi.config.security.rest.service.RefreshTokenService;
import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.LoginRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.LoginResponse;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.RefreshTokenRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.RefreshTokenResponse;
import br.com.wslima.javaspringbootrestapi.rest.dto.user.CreateUserDTO;
import br.com.wslima.javaspringbootrestapi.rest.dto.user.UserInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateAccessToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.refreshToken();

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken);

            if (refreshTokenService.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(403).body(null); // Forbidden: refresh token expirado
            }

            User user = refreshToken.getUser();

            refreshTokenService.deleteByUser(user);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

            String newAccessToken = jwtUtils.generateTokenFromEmail(user.getEmail());

            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, newRefreshToken.getToken()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(null);
        }
    }

    @GetMapping("/me")
    public UserInfoResponse me(Authentication authentication) {
        logger.info("AUTHENTICATION {}", "OLA");
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return new UserInfoResponse(user.getName(), user.getEmail(), roles);
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody CreateUserDTO createUserDTO) {
        if (userRepository.findByEmail(createUserDTO.email()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado.");
        }

        User user = new User();
        user.setEmail(createUserDTO.email());
        user.setName(createUserDTO.name());
        user.setPassword(passwordEncoder.encode(createUserDTO.password()));
        user.setRoles(Set.of(ERole.USER)); // Por padrão, todo novo usuário é USER

        userRepository.save(user);

        return ResponseEntity.ok("Usuário registrado com sucesso.");
    }
}
