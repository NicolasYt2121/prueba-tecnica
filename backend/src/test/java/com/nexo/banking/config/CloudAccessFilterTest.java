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
        assertEquals(401, run("GET", "/", null, null, false).getStatus());
        assertEquals(401, run("GET", "/api/customers", null, null, false).getStatus());
    }
    @Test void rejectsInvalidCredentials() throws Exception {
        assertEquals(401, run("GET", "/", "Basic !!!", null, false).getStatus());
        assertEquals(401, run("GET", "/", auth("wrong"), null, false).getStatus());
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
}
