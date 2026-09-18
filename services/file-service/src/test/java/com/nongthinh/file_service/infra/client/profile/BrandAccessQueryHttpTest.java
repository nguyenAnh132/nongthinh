package com.nongthinh.file_service.infra.client.profile;

import com.nongthinh.file_service.configuration.BrandAccessProperties;
import com.nongthinh.file_service.common.exception.ErrorCode;
import com.nongthinh.file_service.domain.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import static org.junit.jupiter.api.Assertions.*;

class BrandAccessQueryHttpTest {
    @Test
    void readsLiveProfileContractAndFailsClosedForUnavailableOrMalformedResponse() throws Exception {
        var body = new AtomicReference<>("""
                {"code":"1000","message":"ok","traceId":"test","result":{
                  "active":true,"canEditProfile":true,"canSubmitDocuments":false}}
                """);
        var authorization = new AtomicReference<String>();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/profile/brand-profiles/me/access", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] bytes = body.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        try {
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(
                    Jwt.withTokenValue("test-only-token").header("alg", "none").subject("test-user").build()));
            var query = new BrandAccessQueryImpl(new BrandAccessProperties(
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/profile", 1000, 1000));
            assertTrue(query.getCurrentAccess().active());
            assertEquals("Bearer test-only-token", authorization.get());
            body.set("""
                    {"code":"1000","result":{"active":false,"canEditProfile":false,"canSubmitDocuments":false}}
                    """);
            assertFalse(query.getCurrentAccess().active());
            body.set("""
                    {"code":"1000","result":{"active":true}}
                    """);
            assertEquals(ErrorCode.BRAND_ACCESS_UNAVAILABLE,
                    assertThrows(BusinessException.class, query::getCurrentAccess).getErrorCode());
            server.stop(0);
            assertEquals(ErrorCode.BRAND_ACCESS_UNAVAILABLE,
                    assertThrows(BusinessException.class, query::getCurrentAccess).getErrorCode());
        } finally {
            server.stop(0);
            SecurityContextHolder.clearContext();
        }
    }
}
