package com.nexo.banking.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class CloudAccessFilterTest {
    private final CloudAccessFilter filter = new CloudAccessFilter("demo", "test-only-password-123");

    private MockHttpServletResponse run(String method, String path, String auth, String csrf,
                                       boolean expectedPass) throws Exception {
        var request = new MockHttpServletRequest(method, path);
        var response = new MockHttpServletResponse();
        if (auth != null) request.addHeader("Authorization", auth);
        if (csrf != null) request.addHeader("X-Nexo-Request", csrf);
        var passed = new AtomicBoolean();
        filter.doFilter(request, response, (req, res) -> passed.set(true));
        assertEquals(expectedPass, passed.get());
        return response;
    }
    private String auth(String password) {
        return "Basic " + Base64.getEncoder().encodeToString(("demo:" + password).getBytes(StandardCharsets.UTF_8));
    }
    @Test void requiresCredentialsForPageAndApi() throws Exception {
        assertEquals("/login", run("GET", "/", null, null, false).getRedirectedUrl());
        assertEquals(401, run("GET", "/api/customers", null, null, false).getStatus());
    }
    @Test void rejectsInvalidCredentials() throws Exception {
        assertEquals(401, run("GET", "/api/customers", "Basic !!!", null, false).getStatus());
        assertEquals(401, run("GET", "/api/customers", auth("wrong"), null, false).getStatus());
    }
    @Test void permitsAuthenticatedReads() throws Exception {
        assertEquals(200, run("GET", "/api/customers", auth("test-only-password-123"), null, true).getStatus());
    }
    @Test void rejectsCrossSiteFormWrites() throws Exception {
        assertEquals(403, run("POST", "/api/customers", auth("test-only-password-123"), null, false).getStatus());
        assertEquals(200, run("POST", "/api/customers", auth("test-only-password-123"), "1", true).getStatus());
    }
    @Test void onlyHealthGetIsPublic() throws Exception {
        assertEquals(200, run("GET", "/actuator/health", null, null, true).getStatus());
        assertEquals(401, run("POST", "/actuator/health", null, null, false).getStatus());
    }
    @Test void failsClosedForWeakConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new CloudAccessFilter("demo", "short"));
    }
    @Test void browserLoginRotatesSessionAndGrantsAccess() throws Exception {
        var page = new MockHttpServletRequest("GET", "/login");
        var html = new MockHttpServletResponse();
        filter.doFilter(page, html, (req, res) -> fail("Login should be handled by filter"));
        assertTrue(html.getContentAsString().contains("Iniciar sesión"));
        var oldSession = (org.springframework.mock.web.MockHttpSession) page.getSession();
        String firstTabToken = (String) oldSession.getAttribute("nexo.loginCsrf");
        var secondTab = new MockHttpServletRequest("GET", "/login");
        secondTab.setSession(oldSession);
        filter.doFilter(secondTab, new MockHttpServletResponse(), (req, res) -> fail());
        assertEquals(firstTabToken, oldSession.getAttribute("nexo.loginCsrf"),
                "Opening another tab must not invalidate the first tab's form");
        var post = new MockHttpServletRequest("POST", "/login");
        post.setSession(oldSession);
        post.setParameter("csrf", firstTabToken);
        post.setParameter("username", "demo");
        post.setParameter("password", "test-only-password-123");
        var response = new MockHttpServletResponse();
        filter.doFilter(post, response, (req, res) -> fail());
        assertEquals(303, response.getStatus());
        assertTrue(oldSession.isInvalid());
        var next = new MockHttpServletRequest("GET", "/api/customers");
        next.setSession((org.springframework.mock.web.MockHttpSession) post.getSession());
        var passed = new AtomicBoolean();
        filter.doFilter(next, new MockHttpServletResponse(), (req, res) -> passed.set(true));
        assertTrue(passed.get());
    }
    @Test void browserLoginRejectsMissingCsrfAndWrongPassword() throws Exception {
        assertEquals(403, run("POST", "/login", null, null, false).getStatus());
        var page = new MockHttpServletRequest("GET", "/login");
        filter.doFilter(page, new MockHttpServletResponse(), (req, res) -> fail());
        var post = new MockHttpServletRequest("POST", "/login");
        post.setSession((org.springframework.mock.web.MockHttpSession) page.getSession());
        post.setParameter("csrf", (String) page.getSession().getAttribute("nexo.loginCsrf"));
        post.setParameter("username", "demo");
        post.setParameter("password", "incorrecta");
        var response = new MockHttpServletResponse();
        filter.doFilter(post, response, (req, res) -> fail());
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("incorrectos"));
        assertNull(post.getSession().getAttribute("nexo.authenticated"));
    }
}
