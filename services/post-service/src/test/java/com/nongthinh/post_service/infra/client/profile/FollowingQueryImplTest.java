package com.nongthinh.post_service.infra.client.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.ProfileServiceProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class FollowingQueryImplTest {
    private static final UUID VIEWER = new UUID(1, 1);
    private static final UUID FIRST = new UUID(1, 2);
    private static final UUID SECOND = new UUID(1, 3);
    private HttpServer server;
    private FollowingQueryImpl query;
    private final List<String> requests = new ArrayList<>();
    private final List<String> authorizations = new ArrayList<>();
    private List<String> bodies;
    private int status;

    @BeforeEach
    void setup() throws Exception {
        status = 200;
        bodies = List.of("{\"result\":{\"items\":[],\"page\":0,\"hasNext\":false}}");
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/profile/users/", exchange -> {
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            String body = bodies.get(Math.min(requests.size(), bodies.size() - 1));
            requests.add(exchange.getRequestURI().toString());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        query = new FollowingQueryImpl(new ProfileServiceProperties(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/profile", 1000, 1000));
        Jwt jwt = Jwt.withTokenValue("test-token").header("alg", "none").subject("test-user").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        if (server != null) server.stop(0);
    }

    @Test
    void readsAllFollowingPagesUsingTheAuthenticatedToken() {
        bodies = List.of(
                "{\"result\":{\"items\":[{\"userId\":\"" + FIRST + "\",\"displayName\":\"Farmer\"}],\"page\":0,\"size\":100,\"hasNext\":true}}",
                "{\"result\":{\"items\":[{\"userId\":\"" + SECOND + "\"}],\"page\":1,\"size\":100,\"hasNext\":false}}");
        assertThat(query.findFollowingUserIds(VIEWER)).containsExactlyInAnyOrder(FIRST, SECOND);
        assertThat(requests).containsExactly("/profile/users/" + VIEWER + "/following?page=0&size=100",
                "/profile/users/" + VIEWER + "/following?page=1&size=100");
        assertThat(authorizations).containsExactly("Bearer test-token", "Bearer test-token");
    }

    @Test
    void emptyFollowingReturnsAnEmptySet() {
        assertThat(query.findFollowingUserIds(VIEWER)).isEmpty();
    }

    @Test
    void downstreamFailureIsNotTreatedAsEmptyFollowing() {
        status = 503;
        assertError(ErrorCode.PROFILE_SERVICE_UNAVAILABLE);
    }

    @Test
    void downstreamAuthorizationFailureRemainsForbidden() {
        status = 403;
        assertError(ErrorCode.FORBIDDEN);
    }

    @Test
    void rejectsBrokenPaginationInsteadOfLoopingForever() {
        bodies = List.of("{\"result\":{\"items\":[],\"page\":0,\"hasNext\":true}}");
        assertError(ErrorCode.PROFILE_SERVICE_UNAVAILABLE);
        assertThat(requests).hasSize(1);
    }

    private void assertError(ErrorCode code) {
        assertThatThrownBy(() -> query.findFollowingUserIds(VIEWER))
                .isInstanceOfSatisfying(BusinessException.class, error -> assertThat(error.getErrorCode()).isEqualTo(code));
    }
}
