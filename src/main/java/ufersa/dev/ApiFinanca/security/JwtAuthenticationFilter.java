package ufersa.dev.ApiFinanca.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Ignora requisições OPTIONS (preflight CORS) - elas não precisam de autenticação
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
            logger.info("Token extraído com sucesso. Username: {}", username);
        } catch (JwtException ex) {
            logger.error("Erro ao extrair username do token: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("UserDetails carregado para username: {}", username);
                try {
                    if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        logger.info("Autenticação configurada com sucesso para: {}", username);
                    } else {
                        logger.error("Token inválido para username: {} - Token subject: {}, UserDetails username: {}", 
                                username, username, userDetails.getUsername());
                    }
                } catch (JwtException ex) {
                    logger.error("Erro ao validar token para username {}: {}", username, ex.getMessage(), ex);
                    // Token inválido ou expirado - continua sem autenticação
                    // O Spring Security retornará 403 se o endpoint requer autenticação
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                logger.error("Usuário não encontrado: {}", username, ex);
                // Se houver erro ao carregar usuário, continua sem autenticação
                // O Spring Security retornará 403 se o endpoint requer autenticação
            } catch (Exception ex) {
                logger.error("Erro inesperado ao processar autenticação para username {}: {}", username, ex.getMessage(), ex);
                // Se houver erro ao carregar usuário, continua sem autenticação
                // O Spring Security retornará 403 se o endpoint requer autenticação
            }
        } else {
            if (username == null) {
                logger.warn("Username é null após extração do token");
            } else {
                logger.info("Autenticação já existe no contexto para: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }
}

