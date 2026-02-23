package ufersa.dev.ApiFinanca.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ufersa.dev.ApiFinanca.security.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/api/auth/email/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                      //  .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/usuario", "/usuario/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/usuario", "/usuario/**").permitAll()
                        .requestMatchers("/transacao", "/transacao/**").permitAll()
                        .requestMatchers("/categoria", "/categoria/**").permitAll()
                        .requestMatchers("/transacao-recorrente", "/transacao-recorrente/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/onboarding/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }
    
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            logger.error("=== 401 UNAUTHORIZED ===");
            logger.error("Path: {}", request.getRequestURI());
            logger.error("Method: {}", request.getMethod());
            logger.error("Mensagem: {}", authException.getMessage());
            logger.error("Causa: Requisição sem autenticação ou token inválido");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.error("Autenticação no contexto: {}", auth != null ? "PRESENTE" : "AUSENTE");
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token de autenticação ausente ou inválido\"}");
        };
    }
    
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            logger.error("=== 403 FORBIDDEN ===");
            logger.error("Path: {}", request.getRequestURI());
            logger.error("Method: {}", request.getMethod());
            logger.error("Mensagem: {}", accessDeniedException.getMessage());
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                logger.error("Autenticação presente mas sem permissão - Principal: {}", auth.getPrincipal());
                logger.error("Authorities: {}", auth.getAuthorities());
            } else {
                logger.error("❌❌❌ AUTENTICAÇÃO AUSENTE - Este é o motivo do 403!");
                logger.error("❌ O endpoint requer autenticação mas nenhuma foi encontrada no SecurityContext");
            }
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Acesso negado. Token de autenticação necessário.\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite origens de localhost, rede local, domínios do Railway e Firebase Hosting
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://192.168.*.*:*",
            "http://10.*.*.*:*",
            "http://172.16.*.*:*",
            "https://*.railway.app",
            "https://*.up.railway.app",
            "https://financaplus.web.app",
            "https://*.web.app",
            "https://*.firebaseapp.com"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Adiciona os headers permitidos (incluindo usuarioid que é usado pelo frontend)
        configuration.setAllowedHeaders(List.of(
            "Authorization", 
            "Content-Type", 
            "Accept", 
            "X-Requested-With",
            "usuarioid", 
            "usuarioId"
        ));
        // Headers expostos que o frontend pode acessar
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

