package com.nongthinh.post_service.infra.client.profile;

import com.nongthinh.post_service.application.port.out.FollowingQuery;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.common.response.ApiResponse;
import com.nongthinh.post_service.configuration.ProfileServiceProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FollowingQueryImpl implements FollowingQuery {
    private static final int PAGE_SIZE = 100;
    private final RestClient client;

    public FollowingQueryImpl(ProfileServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        client = RestClient.builder().baseUrl(properties.url()).requestFactory(factory).build();
    }

    @Override
    public Set<UUID> findFollowingUserIds(UUID viewerId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !jwt.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        Set<UUID> ids = new HashSet<>();
        try {
            for (int page = 0; ; page++) {
                ApiResponse<FollowingPageDto> response = client.get()
                        .uri("/users/{userId}/following?page={page}&size={size}", viewerId, page, PAGE_SIZE)
                        .headers(headers -> headers.setBearerAuth(jwt.getToken().getTokenValue()))
                        .retrieve().body(new ParameterizedTypeReference<>() { });
                if (response == null || response.getResult() == null) {
                    throw new BusinessException(ErrorCode.PROFILE_SERVICE_UNAVAILABLE);
                }
                FollowingPageDto result = response.getResult();
                if (result.page() != page || result.items() == null
                        || result.items().stream().anyMatch(item -> item == null || item.userId() == null)) {
                    throw new BusinessException(ErrorCode.PROFILE_SERVICE_UNAVAILABLE);
                }
                int previousSize = ids.size();
                result.items().forEach(item -> ids.add(item.userId()));
                if (!result.hasNext()) return Set.copyOf(ids);
                // Do not loop forever if a downstream pagination contract is broken.
                if (ids.size() == previousSize) throw new BusinessException(ErrorCode.PROFILE_SERVICE_UNAVAILABLE);
            }
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, ex);
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new BusinessException(ErrorCode.FORBIDDEN, ex);
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.PROFILE_SERVICE_UNAVAILABLE, ex);
        }
    }
}
