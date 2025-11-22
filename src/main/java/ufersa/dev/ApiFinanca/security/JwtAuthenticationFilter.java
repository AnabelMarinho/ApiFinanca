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
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
            logger.debug("Token extraído com sucesso. Username: {}", username);
        } catch (JwtException ex) {
            logger.warn("Erro ao extrair username do token: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("UserDetails carregado para username: {}", username);
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
                        logger.debug("Autenticação configurada com sucesso para: {}", username);
                    } else {
                        logger.warn("Token inválido para username: {}", username);
                    }
                } catch (JwtException ex) {
                    logger.warn("Erro ao validar token para username {}: {}", username, ex.getMessage());
                    // Token inválido ou expirado - continua sem autenticação
                    // O Spring Security retornará 403 se o endpoint requer autenticação
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                logger.error("Usuário não encontrado: {}", username);
                // Se houver erro ao carregar usuário, continua sem autenticação
                // O Spring Security retornará 403 se o endpoint requer autenticação
            } catch (Exception ex) {
                logger.error("Erro inesperado ao processar autenticação para username {}: {}", username, ex.getMessage(), ex);
                // Se houver erro ao carregar usuário, continua sem autenticação
                // O Spring Security retornará 403 se o endpoint requer autenticação
            }
        }

        filterChain.doFilter(request, response);
    }
}

