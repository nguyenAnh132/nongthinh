package com.nongthinh.brand_service.infra.client.profile;

import com.nongthinh.brand_service.application.port.out.BrandAccessQuery;
import com.nongthinh.brand_service.application.view.BrandAccessView;
import com.nongthinh.brand_service.common.exception.ErrorCode;
import com.nongthinh.brand_service.configuration.BrandAccessProperties;
import com.nongthinh.brand_service.domain.exception.BusinessException;
import com.nongthinh.brand_service.infra.exception.InfrastructureException;
import java.time.Duration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BrandAccessQueryImpl implements BrandAccessQuery {
    private final RestClient client;

    public BrandAccessQueryImpl(BrandAccessProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        client = RestClient.builder().baseUrl(properties.url()).requestFactory(factory).build();
    }

    @Override
    public BrandAccessView getCurrentAccess() {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt)) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }
        try {
            // Forward the verified identity. Never cache an approval decision: revocation must take effect immediately.
            AccessResponse response = client.get().uri("/brand-profiles/me/access")
                    .headers(headers -> headers.setBearerAuth(jwt.getToken().getTokenValue()))
                    .retrieve().body(new ParameterizedTypeReference<>() { });
            if (response == null || !"1000".equals(response.code()) || response.result() == null) {
                throw new InfrastructureException(ErrorCode.BRAND_ACCESS_UNAVAILABLE);
            }
            AccessDto access = response.result();
            if (access.active() == null || access.canEditProfile() == null || access.canSubmitDocuments() == null) {
                throw new InfrastructureException(ErrorCode.BRAND_ACCESS_UNAVAILABLE);
            }
            return new BrandAccessView(access.active(), access.canEditProfile(), access.canSubmitDocuments());
        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, ex);
        } catch (HttpClientErrorException.Forbidden ex) {
            throw new BusinessException(ErrorCode.BRAND_ACCESS_DENIED, ex);
        } catch (RestClientException ex) {
            throw new InfrastructureException(ErrorCode.BRAND_ACCESS_UNAVAILABLE, ex);
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public record AccessResponse(String code, AccessDto result) { }

    public record AccessDto(Boolean active, Boolean canEditProfile, Boolean canSubmitDocuments) { }
}
