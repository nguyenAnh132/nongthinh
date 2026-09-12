package com.nongthinh.agri_catalog_service.infra.client.fileservice;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;
import com.nongthinh.agri_catalog_service.application.port.out.FileServicePort;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileServiceAdapter implements FileServicePort {

    private final FileServiceClient fileServiceClient;

    @Value("${spring.security.api-key.clients.file-service.api-key}")
    private String fileServiceApiKey;

    @Override
    public FileMetadata getActiveFile(UUID fileId) {
        try {
            FileServiceResponse<FileMetadataDto> response = fileServiceClient.getFileById(
                    fileId,
                    fileServiceApiKey
            );
            if (response == null || response.result() == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
            }
            FileMetadataDto file = response.result();
            return new FileMetadata(
                    file.id(),
                    file.ownerUserId(),
                    file.purpose(),
                    file.originalFileName(),
                    file.contentType(),
                    file.sizeBytes(),
                    file.publicUrl(),
                    file.visibility(),
                    file.status()
            );
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, ex);
        } catch (FeignException ex) {
            throw new BusinessException(ErrorCode.FILE_SERVICE_UNAVAILABLE, ex);
        }
    }
}
