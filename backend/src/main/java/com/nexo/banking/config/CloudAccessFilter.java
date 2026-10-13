package com.nexo.banking.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Access gate for the HTTPS-hosted demonstration; not a multi-user banking identity system. */
@Component
@Profile("cloud")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CloudAccessFilter extends OncePerRequestFilter {
    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final byte[] credentials;

    public CloudAccessFilter(@Value("${APP_ACCESS_USER:nicolas}") String user,
                             @Value("${APP_ACCESS_PASSWORD}") String password) {
        if (user.isBlank() || user.contains(":") || password.length() < 16) {
            throw new IllegalArgumentException("Cloud access requires a username and a password of at least 16 characters");
        }
        credentials = (user + ":" + password).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if ("GET".equals(request.getMethod()) && "/actuator/health".equals(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        if (!authenticated(request.getHeader("Authorization"))) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Nexo Finanzas\", charset=\"UTF-8\"");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        // A cross-origin HTML form cannot add this header. No CORS origins are allowed.
        if (!READ_METHODS.contains(request.getMethod()) && !"1".equals(request.getHeader("X-Nexo-Request"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean authenticated(String header) {
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) return false;
        try {
            return MessageDigest.isEqual(credentials, Base64.getDecoder().decode(header.substring(6)));
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }
}
