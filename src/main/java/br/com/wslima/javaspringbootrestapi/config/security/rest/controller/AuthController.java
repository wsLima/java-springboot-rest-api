package br.com.wslima.javaspringbootrestapi.config.security.rest.controller;

import br.com.wslima.javaspringbootrestapi.config.security.JwtUtils;
import br.com.wslima.javaspringbootrestapi.commons.enums.ERole;
import br.com.wslima.javaspringbootrestapi.config.security.persistence.model.RefreshToken;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.LoginRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.LoginResponse;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.RefreshTokenRequest;
import br.com.wslima.javaspringbootrestapi.config.security.rest.dto.login.RefreshTokenResponse;
import br.com.wslima.javaspringbootrestapi.config.security.rest.service.LoginAttemptService;
import br.com.wslima.javaspringbootrestapi.config.security.rest.service.RefreshTokenService;
import br.com.wslima.javaspringbootrestapi.persistence.model.User;
import br.com.wslima.javaspringbootrestapi.persistence.repository.UserRepository;
import br.com.wslima.javaspringbootrestapi.rest.dto.user.CreateUserDTO;
import br.com.wslima.javaspringbootrestapi.rest.dto.user.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ip = request.getRemoteAddr();

        if (loginAttemptService.isBlocked(ip)) {
            logger.warn("IP bloqueado por excesso de tentativas: {}", ip);
            return ResponseEntity.status(429).body(new LoginResponse(null, "Muitas tentativas. Tente novamente mais tarde."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );

            loginAttemptService.loginSucceeded(ip);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtils.generateAccessToken(userDetails);

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado na base de dados."));

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken()));

        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(ip);
            logger.warn("Tentativa de login inválida para o e-mail: {}", loginRequest.email());
            return ResponseEntity.status(401).body(new LoginResponse(null, "Credenciais inválidas."));
        } catch (Exception ex) {
            loginAttemptService.loginFailed(ip);
            logger.error("Erro inesperado durante login: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new LoginResponse(null, "Erro interno no servidor. Tente novamente mais tarde."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        if (loginAttemptService.isBlocked(ip)) {
            logger.warn("IP bloqueado por excesso de tentativas (refresh): {}", ip);
            return ResponseEntity.status(429).body(new RefreshTokenResponse("Muitas tentativas. Tente novamente mais tarde.", null));
        }

        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken());

            if (refreshTokenService.isTokenExpired(refreshToken)) {
                logger.warn("Refresh token expirado: {}", request.refreshToken());
                return ResponseEntity.status(403).body(new RefreshTokenResponse("Refresh token expirado.", null));
            }

            User user = refreshToken.getUser();
            refreshTokenService.deleteByUser(user);
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

            String newAccessToken = jwtUtils.generateTokenFromEmail(user.getEmail());
            loginAttemptService.loginSucceeded(ip);

            return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, newRefreshToken.getToken()));

        } catch (IllegalArgumentException ex) {
            loginAttemptService.loginFailed(ip);
            logger.warn("Refresh token inválido: {}", request.refreshToken());
            return ResponseEntity.status(403).body(new RefreshTokenResponse("Refresh token inválido.", null));
        } catch (Exception ex) {
            loginAttemptService.loginFailed(ip);
            logger.error("Erro inesperado durante refresh token: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new RefreshTokenResponse("Erro interno no servidor. Tente novamente mais tarde.", null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

            Set<String> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(new UserInfoResponse(user.getName(), user.getEmail(), roles));
        } catch (Exception ex) {
            logger.error("Erro ao obter informações do usuário logado: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Erro ao buscar informações do usuário.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserDTO createUserDTO) {
        try {
            if (userRepository.findByEmail(createUserDTO.email()).isPresent()) {
                logger.warn("Tentativa de cadastro com e-mail já existente: {}", createUserDTO.email());
                return ResponseEntity.badRequest().body("E-mail já cadastrado.");
            }

            User user = new User();
            user.setEmail(createUserDTO.email());
            user.setName(createUserDTO.name());
            user.setPassword(passwordEncoder.encode(createUserDTO.password()));
            user.setRoles(Set.of(ERole.USER)); // Por padrão, todo novo usuário é USER

            userRepository.save(user);

            logger.info("Novo usuário registrado com sucesso: {}", createUserDTO.email());
            return ResponseEntity.ok("Usuário registrado com sucesso.");

        } catch (Exception ex) {
            logger.error("Erro ao registrar usuário: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body("Erro interno ao registrar usuário.");
        }
    }
}
