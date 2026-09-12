package com.nongthinh.agri_catalog_service.infra.client.fileservice;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "file-service",
        url = "${spring.security.api-key.clients.file-service.url}"
)
public interface FileServiceClient {

    @GetMapping("/internal/files/{id}")
    FileServiceResponse<FileMetadataDto> getFileById(
            @PathVariable UUID id,
            @RequestHeader("X-API-KEY") String apiKey
    );
}
