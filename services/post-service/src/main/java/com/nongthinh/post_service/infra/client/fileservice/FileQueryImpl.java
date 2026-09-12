package com.nongthinh.post_service.infra.client.fileservice;

import com.nongthinh.post_service.application.model.FileMetadata;
import com.nongthinh.post_service.application.port.out.FileQuery;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.FileServiceProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.UUID;
import java.time.Duration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public final class FileQueryImpl implements FileQuery {
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final RestClient client;
    private final String apiKey;

    public FileQueryImpl(FileServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));
        this.client = RestClient.builder()
                .baseUrl(properties.url())
                .requestFactory(requestFactory)
                .build();
        this.apiKey = properties.apiKey();
    }

    @Override
    public FileMetadata getById(UUID fileId) {
        try {
            FileServiceResponse<FileMetadataDto> response = client.get()
                    .uri("/internal/files/{id}", fileId)
                    .header(API_KEY_HEADER, apiKey)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() { });
            if (response == null || response.result() == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            FileMetadataDto file = response.result();
            return new FileMetadata(
                    file.id(), file.ownerUserId(), file.purpose(), file.contentType(),
                    file.sizeBytes(), file.publicUrl(), file.visibility(), file.status()
            );
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, ex);
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.FILE_SERVICE_UNAVAILABLE, ex);
        }
    }
}
