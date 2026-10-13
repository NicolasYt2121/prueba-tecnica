package com.nexo.banking.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
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
    private static final String AUTH = "nexo.authenticated";
    private static final String CSRF = "nexo.loginCsrf";

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
        if ("/login".equals(request.getRequestURI())) {
            login(request, response);
            return;
        }
        var session = request.getSession(false);
        boolean signedIn = session != null && Boolean.TRUE.equals(session.getAttribute(AUTH));
        if (!signedIn && !authenticated(request.getHeader("Authorization"))) {
            if ("GET".equals(request.getMethod()) && !request.getRequestURI().startsWith("/api/")) {
                response.sendRedirect("/login");
                return;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"detail\":\"Inicia sesión para continuar.\"}");
            return;
        }
        // A cross-origin HTML form cannot add this header. No CORS origins are allowed.
        if (!READ_METHODS.contains(request.getMethod()) && !"1".equals(request.getHeader("X-Nexo-Request"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if ("GET".equals(request.getMethod())) {
            renderLogin(request, response, "");
            return;
        }
        if (!"POST".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        var session = request.getSession(false);
        String csrf = request.getParameter("csrf");
        if (session == null || csrf == null || !csrf.equals(session.getAttribute(CSRF))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            renderLogin(request, response, "La sesión del formulario venció. Intenta de nuevo.");
            return;
        }
        String user = request.getParameter("username");
        String password = request.getParameter("password");
        if (user == null || password == null || !MessageDigest.isEqual(credentials,
                (user + ":" + password).getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            renderLogin(request, response, "Usuario o contraseña incorrectos.");
            return;
        }
        session.invalidate();
        request.getSession(true).setAttribute(AUTH, true);
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader("Location", "/");
    }

    private void renderLogin(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String token = UUID.randomUUID().toString();
        request.getSession(true).setAttribute(CSRF, token);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("""
            <!doctype html><html lang="es"><head><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>Iniciar sesión · Nexo Finanzas</title>
            <style>
            *{box-sizing:border-box}body{margin:0;background:#eef4f2;color:#17353b;font:16px system-ui,sans-serif;min-height:100vh;display:grid;place-items:center;padding:24px}
            main{width:100%;max-width:430px;background:white;padding:36px;border-radius:20px;box-shadow:0 12px 50px #15353b12}
            .brand{color:#16806b;font-weight:700;letter-spacing:2px}h1{font-size:28px;margin-bottom:8px}p{line-height:1.5;color:#52696e}
            label{display:block;margin:22px 0 8px;font-weight:600}input{width:100%;padding:13px;border:1px solid #9fb8b1;border-radius:8px;font:inherit}
            input:focus{outline:2px solid #16806b;outline-offset:2px}button{width:100%;margin-top:26px;padding:14px;border:0;border-radius:8px;background:#126b59;color:white;font:inherit;font-weight:600;cursor:pointer}
            .error{color:#a32727;font-size:14px}.note{font-size:13px;margin-bottom:0}
            </style></head><body><main><div class="brand">NEXO FINANZAS</div>
            <h1>Iniciar sesión</h1><p>Ingresa tus datos de acceso para abrir la prueba técnica.</p>
            <form method="post" action="/login"><input type="hidden" name="csrf" value="__CSRF__">
            <label for="username">Usuario</label><input id="username" name="username" autocomplete="username" required autofocus>
            <label for="password">Contraseña</label><input id="password" name="password" type="password" autocomplete="current-password" required>
            <p class="error" role="alert">__ERROR__</p><button type="submit">Entrar</button></form>
            <p class="note">Acceso privado a la demostración.</p></main></body></html>
            """.replace("__CSRF__", token).replace("__ERROR__", message));
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
