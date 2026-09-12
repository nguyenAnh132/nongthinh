package com.nongthinh.post_service.infra.client.agricatalog;

import com.nongthinh.post_service.application.port.out.CropTypeQuery;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.AgriCatalogServiceProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public final class CropTypeQueryImpl implements CropTypeQuery {
    private final RestClient client;

    public CropTypeQueryImpl(AgriCatalogServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        this.client = RestClient.builder()
                .baseUrl(properties.url())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Set<UUID> findActiveIds(Set<UUID> cropTypeIds) {
        if (cropTypeIds.isEmpty()) {
            return Set.of();
        }
        try {
            AgriCatalogServiceResponse<List<CropTypeDto>> response = client.get()
                    .uri("/public/crop-types")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            if (response == null || response.result() == null) {
                throw new BusinessException(ErrorCode.AGRI_CATALOG_SERVICE_UNAVAILABLE);
            }
            if (response.result().stream()
                    .anyMatch(item -> item == null || item.id() == null)) {
                throw new BusinessException(ErrorCode.AGRI_CATALOG_SERVICE_UNAVAILABLE);
            }
            return response.result().stream()
                    .filter(Objects::nonNull)
                    .filter(CropTypeDto::active)
                    .map(CropTypeDto::id)
                    .filter(cropTypeIds::contains)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (BusinessException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.AGRI_CATALOG_SERVICE_UNAVAILABLE, ex);
        }
    }
}
