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
import org.springframework.security.core.Authentication;
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
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        logger.info("=== JwtAuthenticationFilter - Nova requisição ===");
        logger.info("Método: {}, Path: {}", method, requestPath);
        
        // Ignora requisições OPTIONS (preflight CORS) - elas não precisam de autenticação
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.debug("Requisição OPTIONS ignorada (preflight CORS)");
            filterChain.doFilter(request, response);
            return;
        }
        
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        logger.info("Header Authorization presente: {}", authHeader != null);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (authHeader == null) {
                logger.warn("⚠️ Header Authorization AUSENTE - Requisição será processada sem autenticação");
                logger.warn("⚠️ Se o endpoint requer autenticação, o Spring Security retornará 403");
            } else {
                logger.warn("⚠️ Header Authorization não começa com 'Bearer ' - Valor: {}", authHeader);
            }
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        logger.info("Token extraído (primeiros 20 caracteres): {}...", 
                token.length() > 20 ? token.substring(0, 20) : token);
        
        String username;
        try {
            username = jwtService.extractUsername(token);
            logger.info("✅ Token extraído com sucesso. Username: {}", username);
        } catch (JwtException ex) {
            logger.error("❌ Erro ao extrair username do token: {}", ex.getMessage());
            logger.error("❌ Requisição continuará sem autenticação - Spring Security pode retornar 403");
            filterChain.doFilter(request, response);
            return;
        }

        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Autenticação existente no contexto: {}", existingAuth != null);
        
        if (username != null && existingAuth == null) {
            logger.info("Processando autenticação para username: {}", username);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("✅ UserDetails carregado para username: {}", username);
                logger.info("Authorities do usuário: {}", userDetails.getAuthorities());
                
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
                        logger.info("✅✅ Autenticação configurada com SUCESSO no SecurityContext para: {}", username);
                    } else {
                        logger.error("❌ Token INVÁLIDO para username: {} - Token subject: {}, UserDetails username: {}", 
                                username, username, userDetails.getUsername());
                        logger.error("❌ Requisição continuará sem autenticação - Spring Security retornará 403");
                    }
                } catch (JwtException ex) {
                    logger.error("❌ Erro ao validar token para username {}: {}", username, ex.getMessage(), ex);
                    logger.error("❌ Token inválido ou expirado - Requisição continuará sem autenticação");
                    logger.error("❌ Spring Security retornará 403 se o endpoint requer autenticação");
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                logger.error("❌ Usuário NÃO ENCONTRADO: {}", username, ex);
                logger.error("❌ Requisição continuará sem autenticação - Spring Security retornará 403");
            } catch (Exception ex) {
                logger.error("❌ Erro INESPERADO ao processar autenticação para username {}: {}", username, ex.getMessage(), ex);
                logger.error("❌ Requisição continuará sem autenticação - Spring Security retornará 403");
            }
        } else {
            if (username == null) {
                logger.warn("⚠️ Username é null após extração do token");
            } else {
                logger.info("ℹ️ Autenticação já existe no contexto para: {}", username);
            }
        }

        Authentication finalAuth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Estado final da autenticação: {}", finalAuth != null ? "PRESENTE" : "AUSENTE");
        if (finalAuth != null) {
            logger.info("Principal final: {}, Authenticated: {}", 
                    finalAuth.getPrincipal(), finalAuth.isAuthenticated());
        }
        logger.info("=== FIM: JwtAuthenticationFilter - Continuando para próximo filtro ===");
        
        filterChain.doFilter(request, response);
    }
}


